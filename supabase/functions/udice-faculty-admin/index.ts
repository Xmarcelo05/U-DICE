import { createClient } from "npm:@supabase/supabase-js@2";

const faculties = new Set([
  "CIENCIAS_VIDA_TECNOLOGIAS", "CIENCIAS_SALUD", "INGENIERIA_INDUSTRIA_CONSTRUCCION",
  "CIENCIAS_ADMINISTRATIVAS_CONTABLES_COMERCIO", "EDUCACION_TURISMO_ARTES_HUMANIDADES",
  "CIENCIAS_SOCIALES_DERECHO_BIENESTAR", "BIENESTAR_UNIVERSITARIO", "INSTITUTO_IDIOMAS",
]);
const uuid = /^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;

const reply = (status: number, message: string) => Response.json({ message }, { status });

Deno.serve(async (request) => {
  if (request.method !== "POST") return reply(405, "Método no permitido.");
  const token = /^Bearer (\S+)$/i.exec(request.headers.get("Authorization") ?? "")?.[1];
  if (!token) return reply(401, "Inicia sesión.");

  const url = Deno.env.get("SUPABASE_URL");
  const keys = Deno.env.get("SUPABASE_SECRET_KEYS");
  const key = (keys ? JSON.parse(keys).default : undefined) ?? Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !key) return reply(500, "La función de administración no está configurada.");

  const server = createClient(url, key, { auth: { persistSession: false, autoRefreshToken: false } });
  const identity = await server.auth.getUser(token);
  if (identity.error || !identity.data.user) return reply(401, "La sesión no es válida.");

  const caller = await server.from("perfiles_udice").select("rol").eq("id", identity.data.user.id).maybeSingle();
  if (caller.error) return reply(503, "No se pudieron verificar los permisos.");
  if (caller.data?.rol !== "ADMIN") return reply(403, "Se requiere una cuenta administradora.");

  let input: Record<string, unknown>;
  try {
    const body = await request.json();
    if (!body || typeof body !== "object" || Array.isArray(body)) return reply(400, "Solicitud inválida.");
    input = body;
  } catch {
    return reply(400, "Solicitud inválida.");
  }

  const password = typeof input.password === "string" ? input.password : "";
  const validPassword = password.length >= 8 && password.length <= 128;
  if (input.action === "create") {
    const email = typeof input.email === "string" ? input.email.trim() : "";
    const name = typeof input.nombre === "string" ? input.nombre.trim() : "";
    const faculty = typeof input.facultad === "string" ? input.facultad : "";
    if (!email || email.length > 254 || !name || name.length > 120 || !faculties.has(faculty) || !validPassword) {
      return reply(400, "Revisa el correo, el nombre, la facultad y la contraseña.");
    }
    const result = await server.auth.admin.createUser({
      email,
      password,
      email_confirm: true,
      app_metadata: { udice_role: "ASO" },
      user_metadata: { nombre: name, facultad: faculty },
    });
    if (result.error?.code === "email_exists") return reply(409, "Ese correo ya está registrado.");
    if (result.error || !result.data.user) return reply(422, "No se pudo crear la cuenta ASO.");
    const profile = await server.from("perfiles_udice")
      .update({ rol: "ASO", facultad: faculty })
      .eq("id", result.data.user.id)
      .in("rol", ["LECTOR", "ASO"])
      .select("rol").single();
    if (profile.error || profile.data?.rol !== "ASO") {
      return reply(503, "Auth creó la cuenta, pero falta confirmar el perfil ASO. Revisa la migración de roles.");
    }
    return reply(201, "Cuenta ASO creada.");
  }

  if (input.action !== "reset" && input.action !== "delete") return reply(400, "Acción inválida.");
  const id = typeof input.id === "string" ? input.id : "";
  if (!uuid.test(id)) return reply(400, "Identificador inválido.");
  const target = await server.from("perfiles_udice").select("rol").eq("id", id).maybeSingle();
  if (target.error) return reply(503, "No se pudo verificar la cuenta.");
  if (target.data?.rol !== "ASO") return reply(404, "No se encontró una cuenta ASO con ese identificador.");

  if (input.action === "reset") {
    if (!validPassword) return reply(400, "La contraseña debe tener de 8 a 128 caracteres.");
    const result = await server.auth.admin.updateUserById(id, { password });
    return result.error ? reply(422, "No se pudo cambiar la contraseña.") : reply(200, "Contraseña actualizada.");
  }

  const result = await server.auth.admin.deleteUser(id);
  return result.error ? reply(422, "No se pudo eliminar la cuenta.") : reply(200, "Cuenta eliminada.");
});
