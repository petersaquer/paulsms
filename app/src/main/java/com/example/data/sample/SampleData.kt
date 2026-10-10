package com.example.data.sample

import com.example.data.model.Chat
import com.example.data.model.ChatType
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.data.model.PollOption
import com.example.data.model.User

object SampleData {
    val currentUser = User(
        displayName = "Paul Usuario",
        phoneNumber = "+34 612 890 123",
        username = "@paul_user",
        bio = "Usando Paul Messenger ⚡ Rápido, seguro y elegante."
    )

    fun initialChats(): List<Chat> = listOf(
        Chat(
            id = "saved_messages",
            title = "Mensajes Guardados",
            subtitle = "Tu nube personal",
            username = "",
            type = ChatType.SAVED_MESSAGES,
            lastMessage = "Nota: Revisar la arquitectura de Compose en Android",
            lastMessageTime = "10:14",
            unreadCount = 0,
            isOnline = false,
            isPinned = true,
            isMuted = false,
            avatarGradientStart = 0xFF2AABEE,
            avatarGradientEnd = 0xFF1976D2,
            pinnedMessageText = "Enlaces clave y notas personales"
        ),
        Chat(
            id = "paul_bot",
            title = "Paul Bot",
            subtitle = "bot",
            username = "@paul_bot",
            type = ChatType.BOT,
            lastMessage = "¡Hola! Estoy listo para ayudarte con comandos y respuestas rápidas.",
            lastMessageTime = "10:12",
            unreadCount = 1,
            isOnline = true,
            isPinned = true,
            isVerified = true,
            avatarGradientStart = 0xFF00C853,
            avatarGradientEnd = 0xFF00796B,
            pinnedMessageText = "Escribe /ayuda para ver todos los comandos interactivos"
        ),
        Chat(
            id = "paul_channel",
            title = "Paul Oficial 🚀",
            subtitle = "142.5K suscriptores",
            username = "@paul_app",
            type = ChatType.CHANNEL,
            lastMessage = "✨ Paul 2.0 ya está aquí: Notas de voz instantáneas y nuevo diseño Telegram.",
            lastMessageTime = "09:45",
            unreadCount = 3,
            isPinned = false,
            isVerified = true,
            memberCount = 142500,
            avatarGradientStart = 0xFF7B1FA2,
            avatarGradientEnd = 0xFF512DA8,
            pinnedMessageText = "Novedades oficiales y lanzamientos de la plataforma Paul"
        ),
        Chat(
            id = "elena",
            title = "Elena Gómez",
            subtitle = "en línea",
            username = "@elena_g",
            type = ChatType.DIRECT,
            lastMessage = "🎤 Mensaje de voz (0:18)",
            lastMessageTime = "09:30",
            unreadCount = 2,
            isOnline = true,
            avatarGradientStart = 0xFFE91E63,
            avatarGradientEnd = 0xFFC2185B
        ),
        Chat(
            id = "dev_community",
            title = "Comunidad Android Kotlin",
            subtitle = "3,420 miembros, 184 en línea",
            username = "@android_devs_es",
            type = ChatType.GROUP,
            lastMessage = "Carlos: ¿Ya probaron la nueva versión con Material You y Compose?",
            lastMessageTime = "09:15",
            unreadCount = 12,
            memberCount = 3420,
            avatarGradientStart = 0xFFFF9800,
            avatarGradientEnd = 0xFFF57C00,
            pinnedMessageText = "Reglas: Respeto mutuo y código limpio."
        ),
        Chat(
            id = "carlos",
            title = "Carlos Mendoza",
            subtitle = "última vez hoy a las 08:45",
            username = "@carlos_m",
            type = ChatType.DIRECT,
            lastMessage = "Perfecto, te envío el enlace del repositorio en un momento.",
            lastMessageTime = "08:42",
            unreadCount = 0,
            isOnline = false,
            avatarGradientStart = 0xFF3F51B5,
            avatarGradientEnd = 0xFF303F9F
        ),
        Chat(
            id = "sofia",
            title = "Sofía Valenzuela",
            subtitle = "en línea",
            username = "@sofia_v",
            type = ChatType.DIRECT,
            lastMessage = "Vota en la encuesta para el almuerzo del equipo 🍕",
            lastMessageTime = "Ayer",
            unreadCount = 0,
            isOnline = true,
            avatarGradientStart = 0xFF00BCD4,
            avatarGradientEnd = 0xFF0097A7
        ),
        Chat(
            id = "tech_channel",
            title = "Tech & IA Noticias",
            subtitle = "89.2K suscriptores",
            username = "@technews_es",
            type = ChatType.CHANNEL,
            lastMessage = "Nuevo hito en modelos multimodales y procesamiento local en móviles.",
            lastMessageTime = "Ayer",
            unreadCount = 0,
            memberCount = 89200,
            avatarGradientStart = 0xFF455A64,
            avatarGradientEnd = 0xFF263238
        )
    )

    fun initialMessages(): Map<String, List<Message>> = mapOf(
        "paul_bot" to listOf(
            Message(
                id = "m_bot_1",
                chatId = "paul_bot",
                text = "¡Hola @paul_user! 👋 Bienvenido a Paul, tu mensajero ultrarrápido y seguro.\n\nComandos disponibles:\n• /ayuda - Lista de comandos\n• /clima - Consulta el clima\n• /chiste - Ríete un rato\n• /info - Información de Paul\n• /sticker - Envía un sticker animado",
                timestamp = "10:10",
                isOutgoing = false,
                senderName = "Paul Bot",
                status = MessageStatus.READ
            ),
            Message(
                id = "m_bot_2",
                chatId = "paul_bot",
                text = "/info",
                timestamp = "10:11",
                isOutgoing = true,
                status = MessageStatus.READ
            ),
            Message(
                id = "m_bot_3",
                chatId = "paul_bot",
                text = "🚀 **Paul Messenger**\nVersión: 2.4.0\nMotor: Jetpack Compose + Kotlin\nSeguridad: Cifrado punto a punto en chats secretos\nNube: Almacenamiento ilimitado sincronizado",
                timestamp = "10:12",
                isOutgoing = false,
                senderName = "Paul Bot",
                status = MessageStatus.READ,
                reactions = mapOf("🔥" to 3, "❤️" to 2)
            )
        ),
        "saved_messages" to listOf(
            Message(
                id = "m_saved_1",
                chatId = "saved_messages",
                text = "Lista de ideas para la app Paul:\n1. Animación fluida de burbujas tipo Telegram\n2. Reproductor de notas de voz con onda de audio interactiva\n3. Búsqueda instantánea con filtros",
                timestamp = "08:15",
                isOutgoing = true,
                status = MessageStatus.READ
            ),
            Message(
                id = "m_saved_2",
                chatId = "saved_messages",
                text = "https://developer.android.com/jetpack/compose",
                timestamp = "09:00",
                isOutgoing = true,
                status = MessageStatus.READ
            ),
            Message(
                id = "m_saved_3",
                chatId = "saved_messages",
                text = "Nota: Revisar la arquitectura de Compose en Android",
                timestamp = "10:14",
                isOutgoing = true,
                status = MessageStatus.READ
            )
        ),
        "paul_channel" to listOf(
            Message(
                id = "m_chan_1",
                chatId = "paul_channel",
                text = "📢 **Actualización Paul v2.0 ya disponible**\n\nNos complace presentar la experiencia definitiva de mensajería Telegram para Android.\n\nNovedades clave:\n• Interfaz rediseñada en Material 3 con tema oscuro profundo y tema claro clásico.\n• Notas de voz con reproductor animado.\n• Encuestas interactivas en vivo.\n• Carpetas y pestañas organizadoras de chats.",
                timestamp = "09:40",
                isOutgoing = false,
                senderName = "Paul Oficial",
                status = MessageStatus.READ,
                reactions = mapOf("🔥" to 1420, "👏" to 865, "❤️" to 2100)
            ),
            Message(
                id = "m_chan_2",
                chatId = "paul_channel",
                text = "✨ Paul 2.0 ya está aquí: Notas de voz instantáneas y nuevo diseño Telegram. ¡Comparte con tus amigos!",
                timestamp = "09:45",
                isOutgoing = false,
                senderName = "Paul Oficial",
                status = MessageStatus.READ,
                reactions = mapOf("🚀" to 540, "🎉" to 320)
            )
        ),
        "elena" to listOf(
            Message(
                id = "m_elena_1",
                chatId = "elena",
                text = "¡Hola Paul! ¿Pudiste revisar el diseño que preparamos para la interfaz?",
                timestamp = "09:10",
                isOutgoing = false,
                senderName = "Elena",
                status = MessageStatus.READ
            ),
            Message(
                id = "m_elena_2",
                chatId = "elena",
                text = "¡Hola Elena! Sí, me encantaron los colores y el estilo de las burbujas.",
                timestamp = "09:15",
                isOutgoing = true,
                status = MessageStatus.READ,
                reactions = mapOf("👍" to 1)
            ),
            Message(
                id = "m_elena_3",
                chatId = "elena",
                text = "Genial, te grabé una breve nota de voz explicando los siguientes pasos:",
                timestamp = "09:28",
                isOutgoing = false,
                senderName = "Elena",
                status = MessageStatus.READ
            ),
            Message(
                id = "m_elena_4",
                chatId = "elena",
                text = "Nota de voz",
                timestamp = "09:30",
                isOutgoing = false,
                senderName = "Elena",
                type = MessageType.VOICE,
                voiceDurationSeconds = 18,
                voiceWaveform = listOf(0.2f, 0.5f, 0.8f, 0.6f, 0.9f, 0.4f, 0.7f, 1.0f, 0.6f, 0.3f, 0.8f, 0.5f, 0.9f, 0.7f, 0.4f, 0.6f, 0.3f, 0.2f),
                status = MessageStatus.READ
            ),
            Message(
                id = "m_elena_5",
                chatId = "elena",
                text = "Celebración Confeti 🎉",
                timestamp = "09:32",
                isOutgoing = false,
                senderName = "Elena",
                type = MessageType.GIF,
                gifUrl = "https://media.giphy.com/media/artj92V8o75VPL7AeQ/giphy.gif",
                status = MessageStatus.READ
            ),
            Message(
                id = "m_elena_view_once",
                chatId = "elena",
                text = "🔑 Clave temporal de acceso al servidor de desarrollo: Alpha#9920",
                timestamp = "09:37",
                isOutgoing = false,
                senderName = "Elena",
                isViewOnce = true,
                hasBeenViewed = false,
                status = MessageStatus.READ
            ),
            Message(
                id = "m_elena_6",
                chatId = "elena",
                text = "📍 Ubicación en tiempo real",
                timestamp = "09:35",
                isOutgoing = false,
                senderName = "Elena",
                type = MessageType.LIVE_LOCATION,
                locationData = LocationData(
                    latitude = 40.415363,
                    longitude = -3.707398,
                    title = "Plaza Mayor",
                    address = "Plaza Mayor, 28012 Madrid, España",
                    isLive = true,
                    liveDurationMinutes = 15,
                    remainingMinutes = 12,
                    accuracyMeters = 5.0f
                ),
                status = MessageStatus.READ
            )
        ),
        "dev_community" to listOf(
            Message(
                id = "m_grp_1",
                chatId = "dev_community",
                text = "Hola a todos, ¿alguien ha probado la renderización de listas en Compose?",
                timestamp = "09:02",
                isOutgoing = false,
                senderName = "Marcos R.",
                senderAvatarGradient = 0xFF009688,
                status = MessageStatus.READ
            ),
            Message(
                id = "m_grp_2",
                chatId = "dev_community",
                text = "Sí, con LazyColumn y keys explícitas el rendimiento es increíble a 120 FPS.",
                timestamp = "09:05",
                isOutgoing = false,
                senderName = "Lucía Tech",
                senderAvatarGradient = 0xFF9C27B0,
                status = MessageStatus.READ,
                reactions = mapOf("🔥" to 5)
            ),
            Message(
                id = "m_grp_3",
                chatId = "dev_community",
                text = "Carlos: ¿Ya probaron la nueva versión con Material You y Compose?",
                timestamp = "09:15",
                isOutgoing = false,
                senderName = "Carlos Mendoza",
                senderAvatarGradient = 0xFF3F51B5,
                status = MessageStatus.READ
            )
        ),
        "carlos" to listOf(
            Message(
                id = "m_carlos_1",
                chatId = "carlos",
                text = "Hola Paul, terminamos la revisión del Pull Request.",
                timestamp = "08:35",
                isOutgoing = false,
                senderName = "Carlos",
                status = MessageStatus.READ
            ),
            Message(
                id = "m_carlos_2",
                chatId = "carlos",
                text = "¡Excelente noticia! Pásame el link cuando puedas.",
                timestamp = "08:40",
                isOutgoing = true,
                status = MessageStatus.READ
            ),
            Message(
                id = "m_carlos_3",
                chatId = "carlos",
                text = "Perfecto, te envío el enlace del repositorio en un momento.",
                timestamp = "08:42",
                isOutgoing = false,
                senderName = "Carlos",
                status = MessageStatus.READ
            )
        ),
        "sofia" to listOf(
            Message(
                id = "m_sofia_1",
                chatId = "sofia",
                text = "¡Hola! Para el almuerzo de hoy organicé esta encuesta:",
                timestamp = "Ayer 12:30",
                isOutgoing = false,
                senderName = "Sofía",
                status = MessageStatus.READ
            ),
            Message(
                id = "m_sofia_2",
                chatId = "sofia",
                text = "Encuesta",
                timestamp = "Ayer 12:31",
                isOutgoing = false,
                senderName = "Sofía",
                type = MessageType.POLL,
                pollData = PollData(
                    id = "poll_lunch",
                    question = "¿Dónde almorzamos hoy?",
                    options = listOf(
                        PollOption(1, "🍕 Pizza Artesanal", 7),
                        PollOption(2, "🍣 Sushi & Ramen", 12),
                        PollOption(3, "🥗 Ensaladas y Bowls", 3),
                        PollOption(4, "🍔 Hamburguesas Gourmet", 9)
                    ),
                    totalVotes = 31,
                    userVotedOptionId = 2,
                    isAnonymous = true
                ),
                status = MessageStatus.READ
            ),
            Message(
                id = "m_sofia_3",
                chatId = "sofia",
                text = "Vota en la encuesta para el almuerzo del equipo 🍕",
                timestamp = "Ayer 12:35",
                isOutgoing = false,
                senderName = "Sofía",
                status = MessageStatus.READ
            )
        )
    )
}
