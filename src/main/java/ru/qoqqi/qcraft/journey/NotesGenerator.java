package ru.qoqqi.qcraft.journey;

import net.minecraft.locale.Language;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NotesGenerator {
	
	private static final String BOOK_AUTHOR = "journey.book.common.author";
	
	static ItemStack createNotes(String stageName) {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		
		addBookInformation(book, stageName);
		
		return book;
	}
	
	private static void addBookInformation(ItemStack book, String stageName) {
		book.addTagElement("pages", createTranslatedPages(stageName));
		book.addTagElement("author", localize(BOOK_AUTHOR));
		book.addTagElement("title", localize(makeKey(stageName, "title")));
	}
	
	private static ListTag createTranslatedPages(String stageName) {
		ListTag pages = new ListTag();
		int pageCount = getPageCount(stageName);
		
		for (int i = 1; i <= pageCount; i++) {
			String key = makeKey(stageName, "pages") + "." + i;
			
			pages.add(localizeAsJson(key));
		}
		
		return pages;
	}
	
	private static StringTag localizeAsJson(String key) {
		MutableComponent component = Component.translatable(key);
		String json = Component.Serializer.toJson(component);
		
		return StringTag.valueOf(json);
	}
	
	private static StringTag localize(String key) {
		String translated = Language.getInstance().getOrDefault(key);
		
		return StringTag.valueOf(translated);
	}
	
	private static int getPageCount(String stageName) {
		Language language = Language.getInstance();
		String prefix = makeKey(stageName, "pages");
		
		return (int) language
				.getLanguageData()
				.keySet()
				.stream()
				.filter(key -> key.startsWith(prefix))
				.count();
	}
	
	private static String makeKey(String stageName, String fieldName) {
		return "journey.book." + stageName + "." + fieldName;
	}
}
