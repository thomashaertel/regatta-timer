/*
 * Copyright (c) 2015. ${USER}
 *
 * Licensed under Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.thomashaertel.regattatimer;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;

/**
 * Created by user on 06.05.15.
 */
public class SettingsActivityOld extends Activity implements WearableListView.ClickListener {

    private WearableListView mListView;

    // Sample dataset for the list
    String[] elements = { "List Item 1", "List Item 2" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get the list component from the layout of the activity
        mListView = (WearableListView) findViewById(R.id.wearable_list);

        // Assign an adapter to the list
        mListView.setAdapter(new WearableListAdapter(this, elements));

        // Set a click listener
        mListView.setClickListener(this);
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        // use this data to complete some action ...
    }

    @Override
    public void onTopEmptyRegionClick() {
    }
}
