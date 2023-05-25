package com.medtroniclabs.opensource.ui.boarding.listener

import com.medtroniclabs.opensource.db.tables.SiteEntity

interface SiteSelectionListener {
    fun onSiteSelected(siteEntity: SiteEntity)
    fun onCancelButtonSelected()
}