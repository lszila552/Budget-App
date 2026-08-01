package com.odyssey.game.data

/** A guaranteed change to the four tracked resources. */
data class ChoiceEffect(
    val crew: Int = 0,
    val supplies: Int = 0,
    val favor: Int = 0,
    val stability: Int = 0,
)

// One strategic option at a stop. [base] always applies; if [riskChance] > 0, fate may also apply [riskEffect].
data class Choice(
    val label: String,
    val base: ChoiceEffect,
    val successText: String,
    val riskChance: Float = 0f,
    val riskEffect: ChoiceEffect = ChoiceEffect(),
    val riskText: String = "",
)

/** One stop on the voyage, a political interlude, or a council-chamber scene in Act 2. */
data class Stop(
    val id: String,
    val title: String,
    val subtitle: String,
    val narrative: String,
    val erosion: Int,
    val isInterlude: Boolean = false,
    val speaker: Character? = null,
    val choices: List<Choice>,
)

data class ChoiceOutcome(
    val effect: ChoiceEffect,
    val fateStruck: Boolean,
    val narrative: String,
)

/** Favor above 50 makes fate kinder; favor below 50 makes it crueler. */
fun resolveChoice(choice: Choice, favor: Int): ChoiceOutcome {
    if (choice.riskChance <= 0f) {
        return ChoiceOutcome(choice.base, fateStruck = false, narrative = choice.successText)
    }
    val bias = (favor - 50) / 300f
    val effectiveRisk = (choice.riskChance - bias).coerceIn(0.05f, 0.95f)
    val fateStruck = kotlin.random.Random.nextFloat() < effectiveRisk
    return if (fateStruck) {
        val combined = ChoiceEffect(
            crew = choice.base.crew + choice.riskEffect.crew,
            supplies = choice.base.supplies + choice.riskEffect.supplies,
            favor = choice.base.favor + choice.riskEffect.favor,
            stability = choice.base.stability + choice.riskEffect.stability,
        )
        ChoiceOutcome(combined, fateStruck = true, narrative = choice.riskText)
    } else {
        ChoiceOutcome(choice.base, fateStruck = false, narrative = choice.successText)
    }
}
