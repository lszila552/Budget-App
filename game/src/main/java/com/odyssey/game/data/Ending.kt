package com.odyssey.game.data

enum class EndingKind { LOST_AT_SEA, THRONE_LOST, OVERTHROWN, CIVIL_WAR, VICTORY }

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

fun loyaltyLostEnding(): Ending = Ending(
    kind = EndingKind.OVERTHROWN,
    title = "Overthrown",
    body = "The household guard melts away, then Eumaeus, then even Telemachus's steadiest men. A king " +
        "who has lost every loyal hand in his own hall is not a king anymore, whatever he still calls " +
        "himself. You reclaimed Ithaca only to lose it a second time — this time for good."
)

fun authorityLostEnding(): Ending = Ending(
    kind = EndingKind.CIVIL_WAR,
    title = "Civil War",
    body = "The peace never holds. Grief, unaddressed, curdles into open feud — noble house against " +
        "noble house, town against palace — and Ithaca tears itself apart in the very hall you bled to " +
        "reclaim. Whatever the bards eventually sing of this reign, it will not be a homecoming."
)

/** The saga's final reckoning — voyage and reign both — weighted toward Authority, the throne's grip. */
fun legacyEnding(loyalty: Int, treasury: Int, piety: Int, authority: Int): Ending {
    val score = authority * 0.4 + loyalty * 0.3 + piety * 0.2 + treasury * 0.1
    return when {
        score >= 75 -> Ending(
            EndingKind.VICTORY,
            title = "A Golden Age for Ithaca",
            body = "The spears lower, the mourning ends, and for the first time since Troy, Ithaca knows " +
                "an ordinary morning. Penelope rules at your side in fact as well as name; Telemachus is " +
                "already spoken of as a king in waiting, not merely a king's son. Bards will sing of the " +
                "voyage for a thousand years — but the historians, quieter and longer-lived than bards, " +
                "will remember the reign that came after it just as well."
        )
        score >= 55 -> Ending(
            EndingKind.VICTORY,
            title = "An Uneasy Peace",
            body = "Ithaca holds together, but it remembers. The blood-price is paid, the oaths are " +
                "sworn, and the market square fills again with ordinary trade instead of armed fathers — " +
                "yet old grudges do not vanish just because a truce was signed. You rule a real kingdom, " +
                "scarred but standing, and that will have to be enough."
        )
        score >= 35 -> Ending(
            EndingKind.VICTORY,
            title = "A Kingdom Divided",
            body = "You hold the throne, but barely, and the island under it is fractured — half grateful " +
                "for a king returned, half grieving sons who will not be avenged twice. Your name is " +
                "spoken with respect in some houses and with a curse in others, and both, this year, are " +
                "equally true."
        )
        else -> Ending(
            EndingKind.VICTORY,
            title = "The Long Vengeance",
            body = "The throne is yours, and it is a hollow, watched thing to sit on. Loyalty is thin, " +
                "the treasury thinner, and the gods have turned their faces from a king who won his hall " +
                "back only to rule it in fear. Twenty years at sea, to come home to this — a peace that " +
                "will not last another generation, let alone a lifetime."
        )
    }
}
