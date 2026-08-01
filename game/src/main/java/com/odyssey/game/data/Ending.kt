package com.odyssey.game.data

enum class EndingKind { LOST_AT_SEA, THRONE_LOST, VICTORY }

data class Ending(
    val kind: EndingKind,
    val title: String,
    val body: String,
)

fun crewLostEnding(): Ending = Ending(
    kind = EndingKind.LOST_AT_SEA,
    title = "Lost at Sea",
    body = "The last of your crew is gone — to monster, storm, or god's wrath — and a captain alone " +
        "cannot sail a ship home. Your oar drifts, unmanned, on a sea that will keep your name only as " +
        "a rumor. Ithaca waits for a king who will never come."
)

fun stabilityLostEnding(): Ending = Ending(
    kind = EndingKind.THRONE_LOST,
    title = "The Suitors' Kingdom",
    body = "Word never reaches Ithaca in time. Penelope's cunning runs out, Telemachus's authority " +
        "collapses under the suitors' weight, and the throne is seized in your name's absence. When " +
        "your ship finally returns — if it ever does — it will return to a kingdom that is no longer yours."
)

/** Weighted so the home-front political struggle (stability) matters most, then the voyage itself. */
fun victoryEnding(crew: Int, supplies: Int, favor: Int, stability: Int): Ending {
    val score = stability * 0.4 + crew * 0.3 + favor * 0.2 + supplies * 0.1
    return when {
        score >= 75 -> Ending(
            EndingKind.VICTORY,
            title = "The Great King Returns",
            body = "The suitors lie dead in your own hall. Penelope, after twenty years, looks into your " +
                "eyes and finally believes it is truly you. Telemachus stands beside you as a man, not a " +
                "boy. Ithaca does not merely survive your absence — it is ready, under your hand, to " +
                "flourish for a generation. Bards will sing of this homecoming for a thousand years."
        )
        score >= 55 -> Ending(
            EndingKind.VICTORY,
            title = "A Hard-Won Homecoming",
            body = "You reclaim your hall, your wife, your son. The scars of twenty years — lost men, " +
                "spent favor, a kingdom that had to fend for itself too long — do not vanish overnight. " +
                "But the throne is yours again, and Ithaca, bruised but standing, begins slowly to heal."
        )
        score >= 35 -> Ending(
            EndingKind.VICTORY,
            title = "A Kingdom on Its Knees",
            body = "You have won the hall, but barely. What crew survived stands thin behind you; the " +
                "treasury the suitors gorged on is not easily refilled; and the dead suitors' kin already " +
                "mutter of blood-price and revenge in the hills above town. You are king again — but a " +
                "king with a great deal of ruling still to do."
        )
        else -> Ending(
            EndingKind.VICTORY,
            title = "A Hollow Throne",
            body = "You sit again on the throne of Ithaca, and it is, technically, a victory. But the " +
                "crew that sailed with you is almost entirely gone, the gods regard you coolly at best, " +
                "and the kingdom you fought so hard to reach is a shadow of the one you left. You have " +
                "come home. What you have come home to is another matter."
        )
    }
}
