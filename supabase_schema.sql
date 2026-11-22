-- ====================================================================
-- SISTEMA DE DIFUSIÓN DE INFORMACIÓN ULEAM (U-DICE)
-- Esquema de Base de Datos para Supabase (Optimizado para Plan Gratuito)
-- ====================================================================

-- 1. Habilitar extensión para UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Tabla de Administradores
-- Usada para gestión de cuentas de facultades (creación, reseteo, eliminación)
CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    nombre TEXT NOT NULL DEFAULT 'Administrador General U-DICE',
    rol TEXT NOT NULL DEFAULT 'ADMIN',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Tabla de Facultades / Cuentas de Publicadores (ASO)
CREATE TABLE IF NOT EXISTS facultades_cuentas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facultad TEXT NOT NULL, -- Ej: 'INGENIERIA_SISTEMAS', 'MEDICINA', etc.
    carrera TEXT,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    nombre_contacto TEXT NOT NULL,
    rol TEXT NOT NULL DEFAULT 'ASO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Tabla de Usuarios Lectores (Estudiantes, Profesores)
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    rol TEXT NOT NULL DEFAULT 'LECTOR', -- 'LECTOR' o 'ASO'
    facultad TEXT NOT NULL,
    carrera TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Tabla de Publicaciones (Avisos de las Facultades)
CREATE TABLE IF NOT EXISTS publicaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    autor_id UUID NOT NULL,
    autor_nombre TEXT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(50) NOT NULL, -- 'EVENTO', 'CONVOCATORIA', 'CONFERENCIA', 'AVISO', 'OTRO'
    facultad VARCHAR(100) NOT NULL,
    alcance VARCHAR(20) NOT NULL DEFAULT 'GENERAL', -- 'GENERAL' o 'CARRERA'
    carrera VARCHAR(100),
    fecha TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Tabla de Reacciones Predefinidas (RF 9 - Freddy)
-- Restricción UNIQUE(publicacion_id, usuario_id) para que cada usuario tenga una sola reacción activa por post
CREATE TABLE IF NOT EXISTS reacciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publicacion_id UUID NOT NULL REFERENCES publicaciones(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL,
    tipo_reaccion VARCHAR(30) NOT NULL DEFAULT 'ME_GUSTA', -- 'ME_GUSTA', 'UTIL', 'IMPORTANTE', 'INTERESANTE'
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_reaccion_usuario_post UNIQUE (publicacion_id, usuario_id)
);

-- 7. Tabla de Preferencias de Usuarios (RF 3 y RF 5 - Freddy)
CREATE TABLE IF NOT EXISTS preferencias_usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    facultad VARCHAR(100) NOT NULL,
    interesado BOOLEAN NOT NULL DEFAULT TRUE,
    alcance VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_usuario_facultad_pref UNIQUE (usuario_id, facultad)
);

-- ====================================================================
-- ÍNDICES DE ALTO RENDIMIENTO (Protección contra consumo excesivo en plan gratuito)
-- ====================================================================
CREATE INDEX IF NOT EXISTS idx_publicaciones_fecha ON publicaciones (fecha DESC);
CREATE INDEX IF NOT EXISTS idx_publicaciones_facultad ON publicaciones (facultad);
CREATE INDEX IF NOT EXISTS idx_publicaciones_categoria ON publicaciones (categoria);
CREATE INDEX IF NOT EXISTS idx_reacciones_post ON reacciones (publicacion_id);
CREATE INDEX IF NOT EXISTS idx_preferencias_usuario ON preferencias_usuarios (usuario_id);

-- ====================================================================
-- POLÍTICAS DE ACCESO PARA APP MÓVIL (U-DICE)
-- ====================================================================
ALTER TABLE IF EXISTS publicaciones DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS reacciones DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS preferencias_usuarios DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS facultades_cuentas DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS usuarios DISABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS admin_users DISABLE ROW LEVEL SECURITY;

ALTER TABLE IF EXISTS publicaciones ADD COLUMN IF NOT EXISTS carrera VARCHAR(100);

-- ====================================================================
-- DATOS SEMILLA (Admin por defecto y Facultades iniciales ULEAM)
-- ====================================================================
-- Admin inicial (contraseña hash demostrativa sha256 de 'admin123' o para auth)
INSERT INTO admin_users (email, password_hash, nombre, rol)
VALUES ('admin@uleam.edu.ec', crypt('admin123', gen_salt('bf')), 'Administrador Central U-DICE', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Facultades representativas de la ULEAM
INSERT INTO facultades_cuentas (facultad, carrera, email, password_hash, nombre_contacto, rol)
VALUES 
    ('FACCI', 'Ingeniería de Software', 'facci@uleam.edu.ec', crypt('facci2026', gen_salt('bf')), 'Asociación FACCI', 'ASO'),
    ('MEDICINA', 'Medicina General', 'medicina@uleam.edu.ec', crypt('medicina2026', gen_salt('bf')), 'Comisión Académica Medicina', 'ASO'),
    ('EDUCACION', 'Pedagogía', 'educacion@uleam.edu.ec', crypt('educacion2026', gen_salt('bf')), 'Coordinación Educación', 'ASO')
ON CONFLICT (email) DO NOTHING;
