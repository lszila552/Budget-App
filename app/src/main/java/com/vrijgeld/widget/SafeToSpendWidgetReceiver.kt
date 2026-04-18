package com.vrijgeld.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SafeToSpendWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SafeToSpendWidget()
}
