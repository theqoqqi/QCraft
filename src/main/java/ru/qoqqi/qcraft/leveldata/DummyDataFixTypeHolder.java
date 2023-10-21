package ru.qoqqi.qcraft.leveldata;

import net.minecraft.util.datafix.DataFixTypes;

/**
 * При создании экземпляра <code>SavedData.Factory</code> необходимо передавать
 * один из <code>DataFixTypes</code>, но они все захардкожены в этом enum'е.
 * Пока что, временно, можно передавать какой-нибудь другой <code>DataFixTypes</code>,
 * который ни на что не повлияет. Проверить, будет ли он на что-то влиять,
 * можно поиском по исходникам <code>", References.LEVEL, "</code>.
 */
public class DummyDataFixTypeHolder {

	static final DataFixTypes VALUE = DataFixTypes.LEVEL;
}
