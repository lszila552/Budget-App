package com.odyssey.game.data

// Act 2 follows Homer's actual ending (Odyssey Book 24): Eupeithes' revolt and Athena's intervention.
val ACT2_ITINERARY: List<Stop> = listOf(

    Stop(
        id = "ithaca_dawn",
        scene = SceneType.PALACE_HALL,
        title = "The Morning After",
        subtitle = "Ithaca, the hall",
        narrative = "The suitors lie dead in the hall. Their blood is still being scrubbed from the " +
            "floor by servants who will not meet your eyes. Twenty years of war and wandering brought " +
            "you here, to this — and now the harder part begins. A king who has only killed is not yet " +
            "a king who can rule.",
        erosion = 3,
        choices = listOf(
            Choice(
                label = "Take up the throne",
                base = ChoiceEffect(),
                successText = "You sit, for the first time in twenty years, in your own seat at the head " +
                    "of your own hall. It does not yet feel like home."
            )
        )
    ),

    Stop(
        id = "council_of_elders",
        scene = SceneType.ASSEMBLY,
        title = "Council of Elders",
        subtitle = "The free men of Ithaca gather",
        narrative = "The town elders and free men of Ithaca gather at your summons, uneasy. A hundred " +
            "and eight of the island's best-born sons are dead by your hand and your son's. Some call it " +
            "justice long overdue. Others call it a massacre they will not soon forgive.",
        erosion = 4,
        choices = listOf(
            Choice(
                label = "Address the assembly, own the killing",
                base = ChoiceEffect(stability = 8, favor = -2),
                successText = "You stand before them and do not flinch from what was done, or why. It is " +
                    "a hard truth, plainly told — and plain truths, however hard, steady a throne more " +
                    "than excuses do."
            ),
            Choice(
                label = "Let \"Mentor\" speak on your behalf",
                base = ChoiceEffect(favor = 6, stability = 4),
                successText = "Your old friend Mentor speaks with unexpected authority, and the assembly " +
                    "quiets in a way that feels almost unnatural — as if some greater will moved through " +
                    "the room."
            ),
            Choice(
                label = "Offer reparations to the grieving families",
                base = ChoiceEffect(supplies = -18, stability = 10),
                successText = "You empty a portion of your own treasury into the hands of the bereaved, " +
                    "unasked. It will not bring back their sons, but empty coffers speak louder than " +
                    "empty apologies."
            ),
        )
    ),

    Stop(
        id = "penelope_counsel",
        scene = SceneType.PALACE_INTERIOR,
        title = "The Queen's Counsel",
        subtitle = "Twenty years, and a kingdom to run",
        narrative = "In your chambers, at last alone with your wife after twenty years, the conversation " +
            "turns from each other to the kingdom neither of you quite recognizes anymore. Penelope has " +
            "run this island's politics — quietly, from behind a loom — for two decades. She is not " +
            "asking your permission to keep doing so.",
        erosion = 4,
        speaker = PENELOPE,
        choices = listOf(
            Choice(
                label = "Rule as full and open partners",
                base = ChoiceEffect(stability = 6, crew = 6),
                successText = "You bring her into every council, openly, as equal — a scandal to some of " +
                    "the old men, and exactly the stability Ithaca needs from a marriage tested by twenty " +
                    "years."
            ),
            Choice(
                label = "Ask her to manage the household, you the throne",
                base = ChoiceEffect(crew = -6, stability = 2),
                successText = "She agrees, outwardly gracious. Something in her eyes suggests you have " +
                    "just made the kind of mistake a wiser king would not make twice."
            ),
            Choice(
                label = "Defer to her read on the nobles",
                base = ChoiceEffect(favor = 2, stability = 8, crew = 2),
                successText = "You have been gone twenty years; she has been here every day of them. " +
                    "Trusting her judgment of who can be won over and who cannot proves, again and again, " +
                    "to be the right call."
            ),
        )
    ),

    Stop(
        id = "telemachus_role",
        scene = SceneType.PALACE_INTERIOR,
        title = "A Son Grown",
        subtitle = "What Telemachus is owed",
        narrative = "Your son found his courage while you were still lost at sea — sailing to Pylos and " +
            "Sparta for word of you, standing in this very hall against a hundred armed men. He is not a " +
            "boy anymore, and he does not want to be treated like one.",
        erosion = 4,
        speaker = TELEMACHUS,
        choices = listOf(
            Choice(
                label = "Name him co-ruler in fact, not just title",
                base = ChoiceEffect(crew = 8, stability = -4),
                successText = "You give him real authority — his own judgments, his own council seat. " +
                    "Some nobles bristle at a prince so young wielding real power; Telemachus himself " +
                    "stands taller for it."
            ),
            Choice(
                label = "Keep him close, decisions your own",
                base = ChoiceEffect(crew = -8, stability = 4),
                successText = "He obeys, as a son does — but something guarded enters his voice from that " +
                    "day on, the tone of a man who has learned not to expect to be trusted."
            ),
            Choice(
                label = "Send him to govern the outer estates",
                base = ChoiceEffect(crew = 4, favor = 2),
                successText = "Away from the hall's daily intrigue, he proves a capable hand with the " +
                    "farms, the herds, and the men who work them — a king's education no palace could " +
                    "teach him."
            ),
        )
    ),

    Stop(
        id = "the_disloyal",
        scene = SceneType.PALACE_INTERIOR,
        title = "A Harder Judgment",
        subtitle = "What loyalty is owed, and to whom",
        narrative = "Eumaeus, the swineherd who never once doubted you, brings a harder matter to your " +
            "door: the household maidservants who took the suitors as lovers, and the goatherd Melanthius " +
            "who spat on you when you were still a beggar in your own hall. What is done with them will " +
            "be remembered longer than the suitors ever were.",
        erosion = 5,
        speaker = EUMAEUS,
        choices = listOf(
            Choice(
                label = "Full judgment, exactly as tradition demands",
                base = ChoiceEffect(stability = 14, favor = -8, crew = -4),
                successText = "The sentence is carried out without mercy or hesitation. Word of it spreads " +
                    "through the island by nightfall — no one doubts anymore what disloyalty costs. No one " +
                    "loves you for it, either."
            ),
            Choice(
                label = "Judge only the clearest traitors",
                base = ChoiceEffect(stability = 6, favor = 2),
                successText = "You draw a harder line than tradition asks and a softer one than some " +
                    "demand — a judgment that satisfies no one completely, and unsettles no one badly " +
                    "either."
            ),
            Choice(
                label = "Mercy: exile instead of death",
                base = ChoiceEffect(favor = 10, stability = -10, crew = -2),
                riskChance = 0.4f,
                riskEffect = ChoiceEffect(stability = -8),
                successText = "You choose banishment over blood, a mercy Eumaeus himself did not expect " +
                    "from you. The gods, it's said, take note of restraint in a victorious king.",
                riskText = "Mercy, to men who remember the suitors' meat still on their own tables, looks " +
                    "very much like weakness. Grumbling turns to open questioning of your judgment within " +
                    "the week."
            ),
        )
    ),

    Stop(
        id = "mentor_warning",
        scene = SceneType.RAMPARTS_NIGHT,
        title = "A Warning on the Ramparts",
        subtitle = "Grey eyes in the torchlight",
        narrative = "Your old friend Mentor finds you alone on the ramparts. There is something not " +
            "quite mortal in the way the torchlight catches those grey eyes tonight. \"Eupeithes gathers " +
            "the fathers of the dead in the market square,\" the old friend says. \"Spears, not " +
            "petitions. They mean to come for you before the moon turns.\"",
        erosion = 5,
        speaker = MENTOR,
        choices = listOf(
            Choice(
                label = "Prepare the household to stand and fight",
                base = ChoiceEffect(crew = 4, supplies = -10),
                successText = "You arm every man in the hall loyal enough to trust with a blade. It is " +
                    "not the homecoming you dreamed of on Calypso's shore, but it is the one Ithaca has " +
                    "given you."
            ),
            Choice(
                label = "Send Telemachus to negotiate a blood-price",
                base = ChoiceEffect(crew = -4, supplies = -14, stability = 6),
                successText = "Your son rides out with gold and careful words, offering the old law's " +
                    "remedy for a killing: payment, not more killing. Eupeithes does not accept — but he " +
                    "does not refuse outright either."
            ),
            Choice(
                label = "Appeal publicly for calm and an open trial",
                base = ChoiceEffect(favor = 6, stability = -6),
                successText = "You call for restraint, for law over vengeance, in the town square itself. " +
                    "It is the argument of a wiser age than this one — and Eupeithes' grief is not, " +
                    "tonight, inclined to listen to it."
            ),
        )
    ),

    Stop(
        id = "the_reckoning",
        scene = SceneType.GATES_BATTLE,
        title = "The Reckoning",
        subtitle = "Eupeithes at the gates",
        narrative = "Eupeithes leads the armed fathers of the dead to your gates at dawn, grief turned " +
            "to spears in his hands. Behind you stand your son, your old swineherd, your father Laertes " +
            "leaning on a borrowed shield — and, at your shoulder, Mentor, whose eyes have never looked " +
            "more like a storm.",
        erosion = 0,
        speaker = EUPEITHES,
        choices = listOf(
            Choice(
                label = "Meet them in open battle",
                base = ChoiceEffect(crew = -15, stability = 10),
                riskChance = 0.4f,
                riskEffect = ChoiceEffect(crew = -20, favor = -10),
                successText = "Blades meet in the square before your gates. It is Laertes — old Laertes! " +
                    "— who strikes down Eupeithes himself, and the fight breaks with its leader dead in " +
                    "the dust.",
                riskText = "The fighting spreads further than anyone intended, kin against kin in the " +
                    "streets of the town you're meant to rule, before it finally, bloodily, burns itself " +
                    "out."
            ),
            Choice(
                label = "Let Athena's peace be made",
                base = ChoiceEffect(favor = -12, stability = 22),
                successText = "Mentor's disguise falls away like smoke — grey-eyed Athena stands revealed " +
                    "on your ramparts, and her voice, and Zeus's own thunderbolt splitting the sky beside " +
                    "her, is not a thing any mortal man argues with. The spears lower. An oath of lasting " +
                    "peace is sworn on the spot, gods and mortals both as witness."
            ),
            Choice(
                label = "Go to Eupeithes yourself, unarmed",
                base = ChoiceEffect(crew = -6, favor = 6, stability = 8),
                riskChance = 0.35f,
                riskEffect = ChoiceEffect(crew = -18),
                successText = "You walk out to a grieving father with empty hands, and speak to him as " +
                    "one man who has buried companions to another. It should not work. It very nearly " +
                    "doesn't. But it does.",
                riskText = "Eupeithes' grief has no room left in it for a king's empty hands or careful " +
                    "words, and the spears come anyway — costing you dearly before the fighting, at last, " +
                    "ends."
            ),
        )
    ),
)
