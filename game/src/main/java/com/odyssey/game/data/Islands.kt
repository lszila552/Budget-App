package com.odyssey.game.data

// "Word from Ithaca" stops are the political layer: they never touch Crew or Supplies, only Favor and Stability.
val ODYSSEY_ITINERARY: List<Stop> = listOf(

    Stop(
        id = "troy",
        scene = SceneType.BURNING_CITY,
        title = "Troy's Shore",
        subtitle = "The war is over",
        narrative = "Ten years of war end in ash and smoke. Troy's towers fall behind you, and your " +
            "ships turn toward home — toward Ithaca, toward Penelope, toward a son you barely know. " +
            "The gods have taken sides today; some favor your cunning, others your pride.",
        erosion = 2,
        choices = listOf(
            Choice(
                label = "Weigh anchor",
                base = ChoiceEffect(),
                successText = "The oars bite the water. Ithaca is far, but the wind is fair — for now."
            )
        )
    ),

    Stop(
        id = "ciconians",
        scene = SceneType.COASTAL_RAID,
        title = "The Ciconians",
        subtitle = "Ismarus",
        narrative = "You make landfall at Ismarus, city of the Ciconians, allies of Troy. Your men eye " +
            "its stores hungrily.",
        erosion = 4,
        choices = listOf(
            Choice(
                label = "Sack the city for supplies",
                base = ChoiceEffect(supplies = 18, favor = -6, stability = -2),
                riskChance = 0.55f,
                riskEffect = ChoiceEffect(crew = -15, supplies = -5),
                successText = "Ismarus burns. Your holds are full, but your men grow reckless, drunk on plunder.",
                riskText = "You lingered too long. The Ciconians rally kinsmen from the interior and fall on " +
                    "you at dawn — your rearguard is cut to pieces before you escape."
            ),
            Choice(
                label = "Take only what you need, then leave",
                base = ChoiceEffect(supplies = 8, favor = 2),
                successText = "A measured raid. You take grain and wine enough, and slip away before dawn, " +
                    "sparing needless blood."
            ),
            Choice(
                label = "Sail past without landing",
                base = ChoiceEffect(supplies = -4, favor = 4, stability = 2),
                successText = "You hold your course. Some grumble at the missed plunder, but Athena's favor " +
                    "does not go unnoticed."
            ),
        )
    ),

    Stop(
        id = "interlude_1",
        scene = SceneType.NIGHT_VISION,
        title = "Word from Ithaca",
        subtitle = "The suitors gather",
        narrative = "A gull-grey dream visits you at sea: in the halls of Ithaca, suitors have begun to " +
            "gather, eyeing your wife, your throne, your son's inheritance. Penelope receives them with " +
            "cold courtesy and a promise — she will choose a husband once she finishes weaving a burial " +
            "shroud for old Laertes. Each night, by torchlight, she quietly unpicks the day's threads.",
        erosion = 2,
        isInterlude = true,
        choices = listOf(
            Choice(
                label = "Pray to Athena for Penelope's cunning",
                base = ChoiceEffect(favor = -5, stability = 10),
                successText = "Grey-eyed Athena hears you. The suitors remain fooled a while longer by the " +
                    "unfinished shroud."
            ),
            Choice(
                label = "Pray for Telemachus's resolve",
                base = ChoiceEffect(favor = -3, stability = 7),
                successText = "Somewhere, your son squares his shoulders, and speaks with more of a king's " +
                    "voice at the feasting table."
            ),
            Choice(
                label = "Save your strength for the sea",
                base = ChoiceEffect(favor = 3, stability = -4),
                successText = "You spend no prayers on Ithaca tonight — only on the waves ahead. Whatever " +
                    "happens at home, you will need every god's favor out here."
            ),
        )
    ),

    Stop(
        id = "lotus_eaters",
        scene = SceneType.TROPICAL_SHORE,
        title = "The Lotus-Eaters",
        subtitle = "A shore of sweet forgetting",
        narrative = "On a soft, sun-drowned coast, a gentle people offer your scouts the honeyed fruit of " +
            "the lotus. Those who eat it forget home, forget grief, forget everything but the sweetness " +
            "of forgetting.",
        erosion = 4,
        choices = listOf(
            Choice(
                label = "Forbid anyone from eating it",
                base = ChoiceEffect(favor = 2),
                successText = "You keep the men aboard the ships, and away from temptation. Discipline holds, " +
                    "if grudgingly."
            ),
            Choice(
                label = "Let a few scouts go ashore",
                base = ChoiceEffect(supplies = 4),
                riskChance = 0.5f,
                riskEffect = ChoiceEffect(crew = -12, favor = -2),
                successText = "Your scouts return with fresh water and fruit, untouched by the lotus's spell.",
                riskText = "Your scouts taste the lotus and must be dragged back weeping; three are lost " +
                    "when a longboat overturns in the confusion."
            ),
            Choice(
                label = "Bind the tempted and flee at once",
                base = ChoiceEffect(crew = -3, stability = 3),
                successText = "An ugly business, but the fleet is at sea again before the island's sweetness " +
                    "can claim any more of you."
            ),
        )
    ),

    Stop(
        id = "cyclops",
        scene = SceneType.CAVE_MOUTH,
        title = "The Cyclops's Cave",
        subtitle = "Polyphemus",
        narrative = "In a cave heaped with cheeses and lambs, the one-eyed giant Polyphemus traps your " +
            "crew behind a boulder no ten men could move, and begins, calmly, to eat them.",
        erosion = 5,
        choices = listOf(
            Choice(
                label = "Blind him and name yourself \"Nobody\"",
                base = ChoiceEffect(crew = -8, supplies = 10, favor = -10),
                riskChance = 0.35f,
                riskEffect = ChoiceEffect(crew = -10, favor = -15),
                successText = "The trick works — his kin, hearing \"Nobody hurts me,\" leave him howling alone. " +
                    "You escape lashed beneath his sheep, holds full of his cheese and wine.",
                riskText = "In your pride you shout your true name back across the water as you flee. " +
                    "Polyphemus hurls a boulder that nearly staves in your hull, and prays to his father " +
                    "Poseidon for vengeance — a curse that will follow this whole voyage."
            ),
            Choice(
                label = "Attempt a quiet escape",
                base = ChoiceEffect(crew = -14, favor = 4),
                successText = "You slip free beneath the flock in near silence, losing good men to his hunger, " +
                    "but earning no god's curse."
            ),
            Choice(
                label = "Fight him outright",
                base = ChoiceEffect(crew = -20),
                riskChance = 0.6f,
                riskEffect = ChoiceEffect(crew = -15, favor = -5),
                successText = "By luck and spear you drive him back long enough to break for the ships, blood " +
                    "on the cave floor behind you.",
                riskText = "The giant is too strong in his own den. You lose far more men forcing your way " +
                    "past him than you can afford."
            ),
        )
    ),

    Stop(
        id = "aeolia",
        scene = SceneType.WINDY_ISLE,
        title = "Aeolia",
        subtitle = "The bag of winds",
        narrative = "Aeolus, keeper of the winds, gifts you a leather bag holding every contrary gust, so " +
            "only a gentle wind for home remains free. Ithaca is nearly in sight when your crew, " +
            "suspecting gold inside, take matters into their own hands.",
        erosion = 5,
        choices = listOf(
            Choice(
                label = "Trust the crew to carry it",
                base = ChoiceEffect(stability = 2),
                riskChance = 0.6f,
                riskEffect = ChoiceEffect(supplies = -15, favor = -5, stability = -6),
                successText = "Discipline holds. The bag stays sealed until Ithaca's own cliffs rise from the sea.",
                riskText = "While you sleep, your men untie the bag, certain it hides treasure. The freed " +
                    "storm hurls you back across the entire sea you had crossed — Ithaca, so close, vanishes " +
                    "behind you again."
            ),
            Choice(
                label = "Keep the bag under your own watch",
                base = ChoiceEffect(crew = -2, favor = 2),
                successText = "You barely sleep the whole crossing, but no hand but yours touches the bag. " +
                    "The winds stay bound."
            ),
            Choice(
                label = "Tell the crew exactly what it holds",
                base = ChoiceEffect(crew = -1, favor = 3, stability = 4),
                successText = "Trusted with the truth, your men keep their distance out of respect rather than " +
                    "fear — a small victory of honesty over suspicion."
            ),
        )
    ),

    Stop(
        id = "laestrygonians",
        scene = SceneType.CLIFF_HARBOR,
        title = "The Laestrygonians",
        subtitle = "A harbor of giants",
        narrative = "In a narrow, cliff-walled harbor, giant cannibals hurl boulders down onto your fleet, " +
            "smashing ship after ship as if they were toys.",
        erosion = 6,
        choices = listOf(
            Choice(
                label = "Send the whole fleet in",
                base = ChoiceEffect(supplies = 6),
                riskChance = 0.7f,
                riskEffect = ChoiceEffect(crew = -30, supplies = -10),
                successText = "By sheer speed you're through the strait before the giants gather in force.",
                riskText = "Boulders rain down. Most of your fleet is smashed to driftwood in the harbor — " +
                    "only your own ship claws free."
            ),
            Choice(
                label = "Send one scout ship first",
                base = ChoiceEffect(crew = -6, stability = 2),
                successText = "The scout ship is lost, but its warning saves the rest of the fleet from " +
                    "entering the trap at all."
            ),
            Choice(
                label = "Avoid the harbor entirely",
                base = ChoiceEffect(supplies = -6, stability = -3),
                successText = "You give the cursed inlet a wide berth. It costs time and stores, but not a " +
                    "single life."
            ),
        )
    ),

    Stop(
        id = "interlude_2",
        scene = SceneType.NIGHT_VISION,
        title = "Word from Ithaca",
        subtitle = "The suitors grow bold",
        narrative = "The suitors feast nightly in your hall now, slaughtering your cattle, drinking your " +
            "wine, pressing Penelope harder. Word reaches you, faint as sea-spray, that Telemachus means " +
            "to sail out in search of news of you — a dangerous, brave thing for a young man to attempt.",
        erosion = 2,
        isInterlude = true,
        choices = listOf(
            Choice(
                label = "Send a sign to hold him at home",
                base = ChoiceEffect(favor = -4, stability = 6),
                successText = "An omen stays your son's hand at the harbor's edge. He will need to be patient " +
                    "a while longer."
            ),
            Choice(
                label = "Let him go — a king must act",
                base = ChoiceEffect(favor = 6, stability = -6),
                successText = "You feel, more than know, that your son has put out to sea. It is a father's " +
                    "risk, and a king's lesson."
            ),
            Choice(
                label = "Petition Zeus to intervene",
                base = ChoiceEffect(favor = -10),
                riskChance = 0.3f,
                riskEffect = ChoiceEffect(stability = -10),
                successText = "The king of gods, moved, sends a small mercy — a rumor that scatters the " +
                    "resolve of the boldest suitors.",
                riskText = "Zeus is unmoved by so small a plea, and you have spent favor you could not spare " +
                    "for nothing."
            ),
        )
    ),

    Stop(
        id = "circe",
        scene = SceneType.ENCHANTED_HALL,
        title = "Circe's Aeaea",
        subtitle = "The sorceress's hall",
        narrative = "The sorceress Circe turns half your crew to swine with a single cup. The rest look to " +
            "you.",
        erosion = 6,
        choices = listOf(
            Choice(
                label = "Use Hermes' herb, confront her directly",
                base = ChoiceEffect(favor = 10, stability = 2),
                successText = "Protected by Hermes' moly, her spell breaks on you. Humbled, Circe restores " +
                    "your men and becomes an ally, sharing counsel for the road ahead."
            ),
            Choice(
                label = "Storm the hall by force",
                base = ChoiceEffect(crew = -10, favor = -4),
                riskChance = 0.4f,
                riskEffect = ChoiceEffect(crew = -10),
                successText = "Blades drawn, you fight through her enchanted beasts and free your men, though " +
                    "it costs you dearly.",
                riskText = "Her magic turns the fight against you before you reach the hall; more men are " +
                    "lost retreating than in the assault."
            ),
            Choice(
                label = "Parley and offer tribute",
                base = ChoiceEffect(supplies = -10, favor = 4),
                successText = "Gifts and honeyed words buy your men's freedom without a drop of blood — and " +
                    "Circe, amused, lets you stay a while to rest and resupply."
            ),
        )
    ),

    Stop(
        id = "underworld",
        scene = SceneType.UNDERWORLD,
        title = "The Underworld",
        subtitle = "The counsel of the dead",
        narrative = "At Circe's counsel, you sail to the edge of the world and pour offerings of blood " +
            "into a pit, calling the shades of the dead. Among them waits the blind prophet Tiresias, who " +
            "alone can tell you the shape of the road home.",
        erosion = 6,
        choices = listOf(
            Choice(
                label = "Consult Tiresias at length",
                base = ChoiceEffect(favor = -6, stability = 8),
                successText = "His prophecy is grim but precise — you learn exactly which dangers to avoid, " +
                    "and what price waits if you do not heed him. Ithaca's fate feels, for a moment, less " +
                    "like chance."
            ),
            Choice(
                label = "Seek your mother's shade instead",
                base = ChoiceEffect(favor = 2, stability = 4),
                successText = "Your mother's shade speaks of grief at home, of a wife's long faithfulness and " +
                    "a son's growing burden — sorrow, but also resolve, fills your chest."
            ),
            Choice(
                label = "Take the shortest counsel and go",
                base = ChoiceEffect(favor = 2, stability = -2),
                successText = "You linger as briefly as the dead allow, unwilling to spend more time here than " +
                    "the voyage can afford."
            ),
        )
    ),

    Stop(
        id = "sirens",
        scene = SceneType.ROCKY_SIRENS,
        title = "The Sirens",
        subtitle = "A song worth dying for",
        narrative = "Their song promises perfect knowledge, perfect memory — and death on the rocks below " +
            "for any sailor who steers toward it.",
        erosion = 5,
        choices = listOf(
            Choice(
                label = "Hear it yourself, bound to the mast",
                base = ChoiceEffect(favor = 6, stability = 2),
                successText = "You alone hear the Sirens' song, straining against your ropes, while your " +
                    "wax-eared crew rows on, unmoved and unharmed."
            ),
            Choice(
                label = "Wax every ear, including your own",
                base = ChoiceEffect(),
                successText = "No one hears a note. It is the safest choice, and the dullest story to tell " +
                    "afterward."
            ),
            Choice(
                label = "Let one bold crewman listen too",
                base = ChoiceEffect(favor = -2),
                riskChance = 0.5f,
                riskEffect = ChoiceEffect(crew = -6),
                successText = "He hears the song and grips the rail white-knuckled, but holds — a strange, " +
                    "proud thing to have survived.",
                riskText = "He tears free of restraining hands and leaps for the rocks before anyone can " +
                    "stop him."
            ),
        )
    ),

    Stop(
        id = "scylla_charybdis",
        scene = SceneType.WHIRLPOOL,
        title = "Scylla and Charybdis",
        subtitle = "A strait with no safe side",
        narrative = "A strait so narrow you must choose: the six-headed monster Scylla on one cliff, or the " +
            "ship-swallowing whirlpool Charybdis on the other. No captain passes both unscathed.",
        erosion = 6,
        choices = listOf(
            Choice(
                label = "Hug Scylla's cliff",
                base = ChoiceEffect(crew = -12),
                successText = "Scylla's heads snatch men screaming from the deck — a horror you watch happen " +
                    "and cannot stop — but the ship itself sails on."
            ),
            Choice(
                label = "Risk the center, between the two",
                base = ChoiceEffect(),
                riskChance = 0.55f,
                riskEffect = ChoiceEffect(crew = -35, supplies = -20),
                successText = "By a hair's breadth of current and nerve, you thread the needle without losing " +
                    "a single soul.",
                riskText = "You misjudge the currents. Charybdis's pull nearly swallows the whole ship — you " +
                    "break free, but barely, and at terrible cost."
            ),
            Choice(
                label = "Pray to Athena before choosing Scylla's side",
                base = ChoiceEffect(crew = -8, favor = -6),
                successText = "Fewer men are lost to Scylla's jaws than you feared — whether from the " +
                    "goddess's favor or your crew's own quick oars, you cannot say."
            ),
        )
    ),

    Stop(
        id = "interlude_3",
        scene = SceneType.NIGHT_VISION,
        title = "Word from Ithaca",
        subtitle = "The shroud unravels",
        narrative = "Antinous, boldest of the suitors, now speaks openly of killing Telemachus before he " +
            "can return with allies. Penelope's ruse with the shroud has been discovered — a treacherous " +
            "maid revealed her nightly unweaving. Time, at home, is nearly spent.",
        erosion = 2,
        isInterlude = true,
        choices = listOf(
            Choice(
                label = "Send urgent prayer for Telemachus",
                base = ChoiceEffect(favor = -8, stability = 8),
                successText = "Athena herself is said to have turned the suitors' ambush ship astray in a " +
                    "sudden fog — your son sails home unharmed."
            ),
            Choice(
                label = "Trust Penelope to hold a little longer",
                base = ChoiceEffect(favor = 2, stability = 3),
                successText = "She meets Antinous's accusations with icy composure and buys, through will " +
                    "alone, a little more time."
            ),
            Choice(
                label = "Steel yourself and simply hurry",
                base = ChoiceEffect(stability = -8),
                successText = "No prayer, no omen — only the wind in your sail and the growing certainty that " +
                    "you must reach Ithaca soon, or not at all."
            ),
        )
    ),

    Stop(
        id = "thrinacia",
        scene = SceneType.SACRED_PASTURE,
        title = "Thrinacia",
        subtitle = "The Cattle of the Sun",
        narrative = "Despite every warning, hunger drives your starving crew onto the island of Helios, " +
            "where his sacred, gleaming cattle graze untouched by any mortal hand.",
        erosion = 7,
        choices = listOf(
            Choice(
                label = "Forbid the cattle, ration what remains",
                base = ChoiceEffect(supplies = -14, crew = -4),
                successText = "You hold the line, though your men go to bed with empty bellies and resentful " +
                    "eyes. No god's wrath falls on you here."
            ),
            Choice(
                label = "Allow slaughter when stores run out",
                base = ChoiceEffect(supplies = 20, favor = -20),
                riskChance = 0.6f,
                riskEffect = ChoiceEffect(crew = -25),
                successText = "Fed at last, the crew's strength returns — though you feel, deep in your " +
                    "bones, that this debt will be collected.",
                riskText = "Helios complains to Zeus himself. A sudden storm at sea, days later, splinters " +
                    "your ship in payment for the sacred cattle — most of your crew is drowned."
            ),
            Choice(
                label = "Leave the island immediately, hungry",
                base = ChoiceEffect(supplies = -10, stability = -2, favor = 4),
                successText = "You force the fleet back to sea against furious protest, unwilling to risk a " +
                    "god's herd — whatever it costs in grumbling bellies."
            ),
        )
    ),

    Stop(
        id = "ogygia",
        scene = SceneType.ISLAND_GROTTO,
        title = "Ogygia",
        subtitle = "Calypso's island",
        narrative = "Shipwrecked and alone, you wash ashore on Calypso's island. The nymph offers you " +
            "comfort, immortality even — and years quietly slip by while Ithaca waits.",
        erosion = 8,
        choices = listOf(
            Choice(
                label = "Accept her hospitality, rest fully",
                base = ChoiceEffect(crew = 10, supplies = 15, stability = -18),
                successText = "You recover fully in her care — body and ships restored — but the years lost " +
                    "weigh on you the moment you think of home."
            ),
            Choice(
                label = "Refuse comfort, demand passage at once",
                base = ChoiceEffect(favor = -8, stability = -4),
                successText = "Your longing for home is plain and constant. Eventually, pressured by Zeus's " +
                    "own messenger Hermes, Calypso relents and helps you leave far sooner than she wished."
            ),
            Choice(
                label = "Bargain: brief rest for a sturdier ship",
                base = ChoiceEffect(crew = 5, supplies = 5, stability = -10, favor = 2),
                successText = "A middle path: enough rest to mend your strength, enough urgency to keep the " +
                    "delay short, and a sturdier vessel than a hasty raft."
            ),
        )
    ),

    Stop(
        id = "phaeacia",
        scene = SceneType.HARBOR_CITY,
        title = "Phaeacia",
        subtitle = "The last friendly shore",
        narrative = "Storm-wrecked again and washed up naked on a foreign shore, you are found by " +
            "Nausicaa, princess of the seafaring Phaeacians, and brought before her father's court — your " +
            "last hope for a ship home.",
        erosion = 5,
        choices = listOf(
            Choice(
                label = "Win them over with your story",
                base = ChoiceEffect(favor = 10, stability = 6),
                successText = "Your tale of ten years' suffering moves the whole hall to tears and awe. King " +
                    "Alcinous grants you not just a ship, but treasure besides."
            ),
            Choice(
                label = "Prove yourself in their games",
                base = ChoiceEffect(crew = 4, favor = 6),
                successText = "A thrown discus silences the young nobles who mocked the shipwrecked stranger. " +
                    "Respect, hard-won, opens doors words alone could not."
            ),
            Choice(
                label = "Simply ask for passage home",
                base = ChoiceEffect(stability = 4, favor = 2),
                successText = "No spectacle, no long tale — only a plain, urgent request. The Phaeacians, " +
                    "moved by simple honesty, agree to carry you home themselves."
            ),
        )
    ),

    Stop(
        id = "ithaca_finale",
        scene = SceneType.PALACE_HALL,
        title = "Ithaca",
        subtitle = "The hall of the suitors",
        narrative = "After twenty years, your ship's keel finally scrapes Ithaca's own sand. In your hall, " +
            "more than a hundred suitors feast on what is left of your kingdom, certain you are dead. " +
            "Penelope has just announced a final contest: whoever can string Odysseus's own great bow and " +
            "shoot an arrow through twelve axe heads will have her hand.",
        erosion = 0,
        choices = listOf(
            Choice(
                label = "Go in disguise, gather allies quietly",
                base = ChoiceEffect(crew = 5, favor = 10, stability = 15),
                successText = "Unrecognized, you test loyalties, gather your son, a faithful herdsman, and " +
                    "old allies to your side — and when the moment comes, every exit from the hall is " +
                    "already sealed."
            ),
            Choice(
                label = "Reveal yourself, storm the hall",
                base = ChoiceEffect(crew = -10, favor = -4, stability = 10),
                successText = "Bow in hand, you cut down the boldest suitors before the rest can even reach " +
                    "for their weapons — a bloody, direct reckoning."
            ),
            Choice(
                label = "Call upon Athena to stand with you",
                base = ChoiceEffect(favor = -15, stability = 20),
                successText = "Grey-eyed Athena herself lends you her strength and terror in the fight — the " +
                    "suitors break and scatter before her divine wrath, and few dare stand against a king " +
                    "with a goddess at his side."
            ),
        )
    ),
)
