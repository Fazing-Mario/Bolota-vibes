package com.example.data.model

enum class AccessorySlot {
    HEAD,
    FACE,
    NECK,
    HAND,
    SKIN
}

data class CustomizationItem(
    val id: String,
    val name: String,
    val slot: AccessorySlot,
    val requiredLevel: Int,
    val description: String,
    val emoji: String
)

data class LevelTitle(
    val level: Int,
    val title: String,
    val requiredXp: Int
)

object LevelSystem {
    val TITLES = listOf(
        LevelTitle(1, "Iniciante dos Hábitos", 0),
        LevelTitle(2, "Aprendiz da Rotina", 150),
        LevelTitle(3, "Broto Dedicado", 350),
        LevelTitle(4, "Guerreiro do Foco", 600),
        LevelTitle(5, "Mestre da Disciplina", 900),
        LevelTitle(6, "Estrela Radiante", 1250),
        LevelTitle(7, "Guardião da Saúde", 1650),
        LevelTitle(8, "Titã Inabalável", 2100),
        LevelTitle(9, "Campeão Supremo", 2600),
        LevelTitle(10, "Lendário", 3150)
    )

    fun getLevelFromXp(xp: Int): Int {
        for (i in TITLES.indices.reversed()) {
            if (xp >= TITLES[i].requiredXp) {
                return TITLES[i].level
            }
        }
        return 1
    }

    fun getTitleForLevel(level: Int): String {
        return TITLES.find { it.level == level }?.title ?: "Mestre dos Hábitos"
    }

    fun getXpRangeForLevel(level: Int): Pair<Int, Int> {
        val current = TITLES.find { it.level == level } ?: TITLES.first()
        val next = TITLES.find { it.level == level + 1 }
        val minXp = current.requiredXp
        val maxXp = next?.requiredXp ?: (minXp + 600)
        return Pair(minXp, maxXp)
    }

    val ALL_ITEMS = listOf(
        // Level 1 (Starter items)
        CustomizationItem("laranja", "Laranja Clássico", AccessorySlot.SKIN, 1, "A cor original e fofa do Bolota.", "🧡"),
        CustomizationItem("laco", "Gravata Borboleta", AccessorySlot.NECK, 1, "Elegante e charmoso para qualquer dia.", "🎀"),

        // Level 2
        CustomizationItem("pessego", "Pêssego Dourado", AccessorySlot.SKIN, 2, "Brilho dourado de quem está pegando o ritmo.", "🍑"),
        CustomizationItem("oculos", "Óculos Escuros", AccessorySlot.FACE, 2, "Estilo puro e foco blindado contra distrações.", "🕶️"),

        // Level 3
        CustomizationItem("gorro", "Gorro de Dormir", AccessorySlot.HEAD, 3, "Para quem valoriza um sono de qualidade às 22:30.", "🌙"),
        CustomizationItem("cafe", "Caneca de Chá Quente", AccessorySlot.HAND, 3, "Hidratação e conforto nas manhãs produtivas.", "☕"),

        // Level 4
        CustomizationItem("menta", "Menta Refrescante", AccessorySlot.SKIN, 4, "Calmaria e clareza mental para a rotina.", "🍃"),
        CustomizationItem("faixa", "Faixa de Treino", AccessorySlot.HEAD, 4, "Energia total para os treinos de força e cardio.", "🥊"),
        CustomizationItem("haltere", "Haltere Fitness", AccessorySlot.HAND, 4, "Disciplina muscular em cada repetição.", "🏋️"),

        // Level 5
        CustomizationItem("capelo", "Capelo Acadêmico", AccessorySlot.HEAD, 5, "Homenagem ao mestrado e aos blocos de estudo.", "🎓"),
        CustomizationItem("livro", "Livro de Estudos", AccessorySlot.HAND, 5, "Sabedoria e consistência acadêmica diária.", "📚"),
        CustomizationItem("oculos_leitura", "Óculos Intelectuais", AccessorySlot.FACE, 5, "Para mergulhar nos artigos científicos.", "👓"),

        // Level 6
        CustomizationItem("lavanda", "Lavanda Zen", AccessorySlot.SKIN, 6, "Paz de espírito após vencer tantas vontades.", "💜"),
        CustomizationItem("escudo", "Escudo da Vontade", AccessorySlot.HAND, 6, "Proteção impenetrável contra impulsos ruins.", "🛡️"),
        CustomizationItem("cachecol", "Cachecol Aconchegante", AccessorySlot.NECK, 6, "Conforto para os dias mais puxados.", "🧣"),

        // Level 7
        CustomizationItem("fone", "Fones do Foco", AccessorySlot.HEAD, 7, "Isolamento total de ruído para estudos profundos.", "🎧"),
        CustomizationItem("varinha", "Varinha do Hábito", AccessorySlot.HAND, 7, "Transformando pequenas ações em grandes vitórias.", "✨"),

        // Level 8
        CustomizationItem("noite", "Noturno Estelar", AccessorySlot.SKIN, 8, "Pele cósmica para quem domina os hábitos noturnos.", "🌌"),
        CustomizationItem("medalha", "Medalha de Ouro", AccessorySlot.NECK, 8, "Distinção de ouro por semanas sem recaídas.", "🥇"),
        CustomizationItem("capa", "Capa de Herói", AccessorySlot.NECK, 8, "Um verdadeiro herói da própria disciplina.", "🦸"),

        // Level 10
        CustomizationItem("coroa", "Coroa Real Suprema", AccessorySlot.HEAD, 10, "A glória máxima do rei da consistência.", "👑")
    )
}

data class AchievementDef(
    val id: String,
    val name: String,
    val goal: String,
    val slot: AccessorySlot,
    val need: Int
)

object AchievementsList {
    val ALL = listOf(
        AchievementDef("laco", "Gravata borboleta", "7 dias com registro", AccessorySlot.NECK, 7),
        AchievementDef("oculos", "Óculos escuros", "7 dias sem um hábito ruim", AccessorySlot.FACE, 7),
        AchievementDef("gorro", "Gorro de dormir", "7 noites deitando até 22:30", AccessorySlot.HEAD, 7),
        AchievementDef("faixa", "Faixa de treino", "10 treinos", AccessorySlot.HEAD, 10),
        AchievementDef("capelo", "Capelo", "10 blocos de foco", AccessorySlot.HEAD, 10),
        AchievementDef("escudo", "Escudo", "10 vontades vencidas", AccessorySlot.HAND, 10),
        AchievementDef("cachecol", "Cachecol", "14 dias sem um hábito ruim", AccessorySlot.NECK, 14),
        AchievementDef("medalha", "Medalha de ouro", "30 dias sem um hábito ruim", AccessorySlot.NECK, 30),
        AchievementDef("coroa", "Coroa", "60 dias sem um hábito ruim", AccessorySlot.HEAD, 60)
    )
}

