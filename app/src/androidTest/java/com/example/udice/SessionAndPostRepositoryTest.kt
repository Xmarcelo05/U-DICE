package com.example.udice

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.udice.data.local.RoomLocalDataSource
import com.example.udice.data.local.SeedDataCallback
import com.example.udice.data.local.UDiceDatabase
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Rol
import com.example.udice.data.repository.PostRepositoryImpl
import com.example.udice.data.repository.SessionRepositoryImpl
import com.example.udice.data.remote.FakeRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionAndPostRepositoryTest {
    @Test
    fun readerCredentialsAndAuthorPermissions() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, UDiceDatabase::class.java)
            .addCallback(SeedDataCallback())
            .build()
        try {
            val io = Dispatchers.IO
            val local = RoomLocalDataSource(db.postDao(), db.userDao(), db.preferenceDao(), db.savedPostDao(), io)
            val session = SessionRepositoryImpl(local, db.userDao(), io)
            val posts = PostRepositoryImpl(local, FakeRemoteDataSource(io), session, io)
            val faculty = Facultad.CIENCIAS_VIDA_TECNOLOGIAS
            val post = checkNotNull(posts.getPostById("post-seed-1"))

            assertNull(session.getCurrentUser())
            session.register("Lector", "lector@example.test", "test1234", faculty, "Software").getOrThrow()
            assertTrue(session.register("Duplicado", "LECTOR@example.test", "test1234", faculty, "Software").isFailure)
            assertTrue(session.login("lector@example.test", "incorrecta").isFailure)
            assertNull(session.getCurrentUser())
            session.login("LECTOR@example.test", "test1234").getOrThrow()
            assertEquals(Rol.LECTOR, session.getCurrentUser()?.rol)
            assertTrue(posts.updatePost(post.copy(titulo = "Cambio prohibido")).isFailure)
            assertTrue(posts.deletePost(post.id).isFailure)
            assertNotNull(posts.getPostById(post.id))

            session.logout().getOrThrow()
            assertNull(session.getCurrentUser())
            assertTrue(posts.deletePost(post.id).isFailure)

            session.login("aso.sistemas@universidad.edu", "UDiceDemo2026!").getOrThrow()
            assertEquals(Rol.ASO, session.getCurrentUser()?.rol)
            val foreign = post.copy(id = "foreign-post", autorId = "other-aso")
            local.insertPost(foreign)
            assertTrue(posts.updatePost(foreign.copy(titulo = "Cambio prohibido")).isFailure)
            assertTrue(posts.deletePost(foreign.id).isFailure)

            posts.updatePost(post.copy(titulo = "Aviso actualizado")).getOrThrow()
            posts.syncPosts().getOrThrow()
            assertEquals("Aviso actualizado", posts.getPostById(post.id)?.titulo)
            posts.deletePost(post.id).getOrThrow()
            posts.syncPosts().getOrThrow()
            assertNull(posts.getPostById(post.id))
        } finally {
            db.close()
        }
    }
}
