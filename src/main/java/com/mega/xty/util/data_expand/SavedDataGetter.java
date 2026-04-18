package com.mega.xty.util.data_expand;

import com.mega.xty.common.data.map1.Game2SavedData;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.map2.Game1SavedData;
import com.mega.xty.common.data.map2.Map2SavedData;

public interface SavedDataGetter {
    Game2SavedData getMap1game2SavedData();
    Game1SavedData getMap2game1SavedData();
    com.mega.xty.common.data.map2.Game2SavedData getMap2game2SavedData();
    Map2SavedData getMap2SavedData();
    FpsSavedData getFpsSavedData();
}
