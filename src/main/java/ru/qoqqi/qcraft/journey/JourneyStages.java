package ru.qoqqi.qcraft.journey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JourneyStages {

	private static final List<JourneyStage> allStages = new ArrayList<>();

	private static final Map<String, JourneyStage> byNames = new HashMap<>();

	public static final JourneyStage TRAVELERS_HOME = register("travelers_home");

	public static final JourneyStage FORTUNE_ISLAND = register("fortune_island");

	public static final JourneyStage JUNGLE_TEMPLE = register("jungle_temple");

	public static final JourneyStage MANGROVE_TEMPLE = register("mangrove_temple");

	public static final JourneyStage PANDORAS_TEMPLE = register("pandoras_temple");

	private static JourneyStage register(String name) {
		JourneyStage stage = new JourneyStage(name, () -> NotesGenerator.createNotes(name));
		allStages.add(stage);
		byNames.put(name, stage);
		return stage;
	}

	public static JourneyStage byName(String name) {
		return byNames.get(name);
	}

	static JourneyStage getPrevious(JourneyStage stage) {
		return tryGet(indexOf(stage) - 1);
	}

	static JourneyStage getNext(JourneyStage stage) {
		return tryGet(indexOf(stage) + 1);
	}

	public static JourneyStage getFirst() {
		return allStages.get(0);
	}

	public static JourneyStage getLast() {
		return allStages.get(allStages.size() - 1);
	}

	private static JourneyStage tryGet(int index) {
		return index >= 0 && index < allStages.size()
				? allStages.get(index)
				: null;
	}

	private static int indexOf(JourneyStage stage) {
		return allStages.indexOf(stage);
	}
}
