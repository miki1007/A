package com.mikix.data

object SeedExercises {
    val list = listOf(
        "Barbell Bench Press" to "Chest", "Incline Dumbbell Press" to "Chest", "Cable Fly" to "Chest", "Push-Up" to "Chest",
        "Overhead Press" to "Shoulders", "Lateral Raise" to "Shoulders", "Rear Delt Fly" to "Shoulders", "Arnold Press" to "Shoulders",
        "Back Squat" to "Legs", "Front Squat" to "Legs", "Romanian Deadlift" to "Legs", "Leg Press" to "Legs", "Lunge" to "Legs",
        "Deadlift" to "Back", "Barbell Row" to "Back", "Pull-Up" to "Back", "Lat Pulldown" to "Back", "Seated Cable Row" to "Back",
        "Barbell Curl" to "Arms", "Hammer Curl" to "Arms", "Triceps Pushdown" to "Arms", "Skull Crusher" to "Arms",
        "Hip Thrust" to "Glutes", "Glute Bridge" to "Glutes", "Calf Raise" to "Calves", "Seated Calf Raise" to "Calves",
        "Plank" to "Core", "Cable Crunch" to "Core", "Hanging Leg Raise" to "Core", "Russian Twist" to "Core",
        "Chest Dip" to "Chest", "Machine Press" to "Chest", "Pec Deck" to "Chest", "Single-arm Press" to "Chest",
        "Machine Shoulder Press" to "Shoulders", "Upright Row" to "Shoulders", "Face Pull" to "Shoulders", "Shrug" to "Shoulders",
        "Hack Squat" to "Legs", "Split Squat" to "Legs", "Leg Extension" to "Legs", "Leg Curl" to "Legs",
        "Good Morning" to "Back", "T-Bar Row" to "Back", "Meadow Row" to "Back", "Straight-arm Pulldown" to "Back",
        "Preacher Curl" to "Arms", "Concentration Curl" to "Arms", "Overhead Triceps Extension" to "Arms", "Close Grip Bench" to "Arms",
        "Cable Kickback" to "Glutes", "Frog Pump" to "Glutes", "Donkey Calf Raise" to "Calves", "Ab Wheel" to "Core",
        "Farmer Carry" to "Core", "Sled Push" to "Legs", "Reverse Hyper" to "Back", "Landmine Press" to "Shoulders",
        "Incline Curl" to "Arms", "JM Press" to "Arms", "Pause Squat" to "Legs", "Deficit Deadlift" to "Back"
    ).mapIndexed { i, pair ->
        Exercise(id = (i + 1).toLong(), name = pair.first, muscleGroup = pair.second, equipment = if (pair.first.contains("Machine")) "Machine" else "Free Weight")
    }
}
