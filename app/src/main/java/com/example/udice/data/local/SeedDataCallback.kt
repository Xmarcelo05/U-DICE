package com.example.udice.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Inserta contenido mínimo al crear la base, disponible incluso sin conexión. */
class SeedDataCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO users (id,nombre,email,rol,facultad,carrera,isCurrentSession) VALUES ('user-aso-1','Asociación Estudiantil Sistemas','aso.sistemas@universidad.edu','ASO','CIENCIAS_VIDA_TECNOLOGIAS','Ingeniería en Computación',0)")
        db.execSQL("INSERT INTO users (id,nombre,email,rol,facultad,carrera,isCurrentSession) VALUES ('user-lector-1','Estudiante Lector','estudiante@universidad.edu','LECTOR','CIENCIAS_VIDA_TECNOLOGIAS','Ingeniería en Computación',0)")
        val now = System.currentTimeMillis()
        db.execSQL("INSERT INTO posts (id,autorId,autorNombre,titulo,descripcion,categoria,facultad,alcance,carrera,fecha,imageUrl) VALUES ('post-seed-1','user-aso-1','Asociación Estudiantil Sistemas','Bienvenida a U-DICE','Explora avisos, eventos y convocatorias de tu comunidad universitaria.','AVISO','CIENCIAS_VIDA_TECNOLOGIAS','GENERAL',NULL,$now,NULL)")
        db.execSQL("INSERT INTO posts (id,autorId,autorNombre,titulo,descripcion,categoria,facultad,alcance,carrera,fecha,imageUrl) VALUES ('post-seed-2','user-aso-1','Asociación Estudiantil Sistemas','Encuentro de comunidades estudiantiles','Próximamente compartiremos actividades y oportunidades para participar.','EVENTO','CIENCIAS_VIDA_TECNOLOGIAS','CARRERA','Ingeniería en Computación',${now - 3600000},NULL)")
    }
}
