package com.medtroniclabs.opensource.ui.landing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.medtroniclabs.opensource.BuildConfig
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.common.DeviceInformation
import com.medtroniclabs.opensource.common.TransferStatusEnum
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientTransferListResponse
import com.medtroniclabs.opensource.data.model.PatientTransferNotificationCountRequest
import com.medtroniclabs.opensource.data.model.PatientTransferUpdateRequest
import com.medtroniclabs.opensource.databinding.LandingActivityBinding
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TagHomeScreen
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TagPrivacyPolicy
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.PatientDetailDialogue
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.boarding.ChooseSiteDialogueFragment
import com.medtroniclabs.opensource.ui.boarding.LoginActivity
import com.medtroniclabs.opensource.ui.boarding.listener.SiteSelectionListener
import com.medtroniclabs.opensource.ui.home.ApproveRejectListener
import com.medtroniclabs.opensource.ui.home.HomeScreenFragment
import com.medtroniclabs.opensource.ui.home.adapter.IncomingRequestAdapter
import com.medtroniclabs.opensource.ui.home.adapter.InformationMessageAdapter
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.screening.TranslationDialogueFragment
import com.medtroniclabs.opensource.uploadservice.UploadForegroundService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LandingActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener, ApproveRejectListener,
    SiteSelectionListener, DrawerLayout.DrawerListener {

    private lateinit var binding: LandingActivityBinding

    private var chooseSiteDialogueFragment: ChooseSiteDialogueFragment? = null

    private val viewModel: LandingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = LandingActivityBinding.inflate(layoutInflater)
        setMainContentView(binding.root)

        initializeDrawerView()

        updateSideBarFooter()

        initializeHomeViews()

        attachObserver()

        onNavigationItemSelected(binding.navView.menu.findItem(R.id.home))

        viewModel.validateSession()
        hideCultureMenu()
    }

    private fun hideCultureMenu() {
        val list = arrayListOf(
            RoleConstant.PROVIDER,
            RoleConstant.PHYSICIAN_PRESCRIBER,
            RoleConstant.LAB_TECHNICIAN,
            RoleConstant.PHARMACIST,
            RoleConstant.NUTRITIONIST,
        )
        if (list.contains(SecuredPreference.getSelectedSiteEntity()?.role) && binding.navView.menu.size() > 0) {
            binding.navView.menu.forEach { menuItem ->
                if(menuItem.itemId == R.id.selectCulture)
                {
                    menuItem.isVisible = false
                    return@forEach
                }
            }
        }
    }

    private fun initializeDrawerView() {
        val toolBar = binding.appBarMain.toolbar
        setSupportActionBar(toolBar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        binding.drawerLayout.addDrawerListener(this)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
    }


    private fun updateSideBarFooter() {
            binding.appBarBottom.tvAppVersion.text = "${getString(R.string.app_version)} ${BuildConfig.VERSION_NAME}"
    }

    /**
     * method to Observe live data
     */

    private fun attachObserver() {
        viewModel.siteListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { siteList ->
                        when {
                            siteList.isNullOrEmpty() -> {
                                showSiteError()
                            }
                        }
                    } ?: kotlin.run {
                        showSiteError()
                    }
                }
            }
        }
        viewModel.siteStoredLiveData.observe(this) { status ->
            if (status) {
                applyUserData()
                onNavigationItemSelected(binding.navView.menu.findItem(R.id.home))
                doRefreshForDataUpdate()
            } else {
                showSiteError()
            }
        }
        viewModel.userLogout.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideLoading()
                    redirectToLogin()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.LOADING -> {
                    showLoading()
                }
            }
        }

        viewModel.dataCount.observe(this) { count ->
            if (count > 0) {
                callSyncService()
            }
        }

        viewModel.switchOrganizationResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    viewModel.selectedSiteEntity?.let {
                        viewModel.storeSelectedEntity(it)
                    }
                }
            }
        }

        viewModel.patientTransferNotificationCountResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }
                ResourceState.ERROR -> {
                    binding.appBarMain.tvNotificationCount.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { dataResponse ->
                        val transferCount = dataResponse.patientTransferCount
                        binding.appBarMain.tvNotificationCount.visibility =
                            if (transferCount > 0) View.VISIBLE else View.GONE
                        binding.appBarMain.tvNotificationCount.text =
                            if (transferCount > 99) getString(R.string.notification_plus) else transferCount.toString()
                    } ?: kotlin.run {
                        binding.appBarMain.tvNotificationCount.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.patientListResponse.observe(this) { resoruceState ->
            when (resoruceState.state) {
                ResourceState.LOADING -> {
                    showHideList(false)
                }
                ResourceState.ERROR -> {
                    showHideList(true)
                }
                ResourceState.SUCCESS -> {
                    showHideList(true)
                    resoruceState.data?.let { data ->
                        loadAdapterData(data)
                    }
                }
            }
        }

        viewModel.patientUpdateResponse.observe(this) { resorceState ->
            when (resorceState.state) {
                ResourceState.LOADING -> {
                    showHideList(false)
                }
                ResourceState.ERROR -> {
                    showHideList(true)
                }
                ResourceState.SUCCESS -> {
                    showHideList(true)
                    viewModel.patientUpdateResponse.setError()
                    binding.drawerLayout.closeDrawer(binding.navNotificationView)
                    resorceState.data?.message?.let { message ->
                        GeneralSuccessDialog.newInstance(this, getString(R.string.transfer), message, needHomeNav = false).
                        show(supportFragmentManager,GeneralSuccessDialog.TAG)
                    }
                }
            }
        }

        viewModel.culturesListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> hideLoading()
            }
        }
    }

    private fun showHideList(status: Boolean) {
        if (status){
            binding.CenterProgress.visibility = View.GONE
            binding.rvOutgoingList.visibility = View.VISIBLE
            binding.rvOutgoingList.visibility = View.VISIBLE
        }else {
            binding.CenterProgress.visibility = View.VISIBLE
            binding.rvOutgoingList.visibility = View.GONE
            binding.rvInformationList.visibility = View.GONE
        }
    }

    private fun loadAdapterData(data: PatientTransferListResponse) {
        if (data.incomingPatientList.size > 0) {
            binding.rvOutgoingList.visibility = View.VISIBLE
            binding.rvOutgoingList.addItemDecoration(
                DividerItemDecoration(
                    baseContext,
                    LinearLayoutManager.VERTICAL
                )
            )
            binding.rvOutgoingList.layoutManager = LinearLayoutManager(this@LandingActivity)
            binding.rvOutgoingList.adapter = IncomingRequestAdapter(data.incomingPatientList, this)
        } else {
            binding.rvOutgoingList.visibility = View.GONE
        }
        if (data.outgoingPatientList.size > 0) {
            binding.rvInformationList.visibility = View.VISIBLE
            binding.rvInformationList.layoutManager = LinearLayoutManager(this@LandingActivity)
            binding.rvInformationList.addItemDecoration(
                DividerItemDecoration(
                    baseContext,
                    LinearLayoutManager.VERTICAL
                )
            )
            binding.rvInformationList.adapter =
                InformationMessageAdapter(data.outgoingPatientList, this)
        } else {
            binding.rvInformationList.visibility = View.GONE
        }
        val totalCount = data.incomingPatientList.size + data.outgoingPatientList.size
        if (totalCount > 0) {
            binding.tvNoNotificationsFound.visibility = View.GONE
            binding.tvDialogTitle.text =
                getString(R.string.notification_count, totalCount.toString())
        } else {
            binding.tvNoNotificationsFound.visibility = View.VISIBLE
            binding.tvDialogTitle.text = getString(R.string.notification)
        }
    }

    private fun redirectToLogin() {
        startAsNewActivity(Intent(this, LoginActivity::class.java))
    }

    private fun showSiteError() {
        /**
         * this method is not used
         */
    }

    /**
     * method to initialize home view toolbar and views
     */
    private fun initializeHomeViews() {
        viewModel.getSiteList(true)
        viewModel.getCulturesList()
        applyUserData()
        binding.appBarMain.ivNotification.safeClickListener(this)
        binding.appBarMain.tvNotificationCount.safeClickListener(this)
        val role = SecuredPreference.getSelectedSiteEntity()?.role
        if (role == RoleConstant.PROVIDER || role == RoleConstant.PHYSICIAN_PRESCRIBER){
            binding.appBarMain.ivNotification.visibility = View.VISIBLE
        }else {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)
            binding.appBarMain.ivNotification.visibility = View.GONE
        }
        binding.ivClose.safeClickListener(this)
    }

    private fun applyUserData() {
        SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
            siteEntity.apply {
                binding.appBarMain.tvTitle.text = name
                binding.appBarMain.tvRole.visibility = View.VISIBLE
                binding.appBarMain.tvRole.text =
                    if (roleName.isNullOrBlank()) getString(R.string.separator_hyphen) else roleName
                role?.let { role ->
                    fetchUserMenuList(role)
                }
            }
        }
    }

    /**
     * Handle navigation view item clicks here.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.userLogout(this)
                } else {
                    SecuredPreference.clear(this)
                    redirectToLogin()
                }
            }
        }
        selectNavigationMenu(item)
        displayScreen(item.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun selectNavigationMenu(item: MenuItem) {
        if (binding.navView.menu.size() > 0) {
            binding.navView.menu.forEach { menuItem ->
                menuItem.isChecked = (menuItem.itemId == item.itemId)
            }
        }
    }

    private fun displayScreen(id: Int) {
        when (id) {
            R.id.home -> {
                applyUserData()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, HomeScreenFragment(), TagHomeScreen).commit()
            }
            R.id.offlineData -> {
                viewModel.fetchOfflineScreenedCount()
                OfflineDataDialog.newInstance { startUpload ->
                    if (startUpload) {
                        if (connectivityManager.isNetworkAvailable())
                            callSyncService()
                        else
                            showErrorDialogue(
                                getString(R.string.error),
                                getString(R.string.no_internet_error),
                                isNegativeButtonNeed = false
                            ) {}
                    }
                    onCancelButtonSelected()
                }.show(supportFragmentManager, OfflineDataDialog.TAG)
            }
            R.id.changeFacility -> {
                viewModel.siteListLiveData.value?.data?.let {
                    showSiteDialogue(it)
                }
            }
            R.id.selectCulture -> {
                viewModel.culturesListLiveData.value?.data?.let {
                    TranslationDialogueFragment.newInstance(ArrayList(it)).show(supportFragmentManager, TranslationDialogueFragment.TAG)
                }
            }
            R.id.privacyPolicy -> {
                if (connectivityManager.isNetworkAvailable()) {
                    binding.appBarMain.tvTitle.text = getString(R.string.privacy_policy)
                    binding.appBarMain.tvRole.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.relativelayout, PrivacyPolicyFragment(), TagPrivacyPolicy)
                        .commit()
                }else {
                    showErrorDialogue(
                        getString(R.string.error), getString(R.string.no_internet_error),
                        false
                    ) {}
                }
            }
        }
    }

    /**
     * OnClickListener
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivNotification, R.id.tvNotificationCount -> {
                binding.drawerLayout.openDrawer(binding.navNotificationView)
            }
            R.id.ivClose -> {
                binding.drawerLayout.closeDrawer(binding.navNotificationView)
            }
        }
    }

    private fun showSiteDialogue(list: List<SiteEntity>) {
        if (connectivityManager.isNetworkAvailable()) {
            chooseSiteDialogueFragment = ChooseSiteDialogueFragment.newInstance(
                ArrayList(list),
                SecuredPreference.getSelectedSiteEntity(), this
            )
            chooseSiteDialogueFragment?.show(supportFragmentManager, ChooseSiteDialogueFragment.TAG)
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                false
            ) {}
        }
    }

    /**
     * SiteSelectionListener
     */
    override fun onSiteSelected(siteEntity: SiteEntity) {
        siteEntity.let {
            viewModel.selectedSiteEntity = it
            val deviceDetails = DeviceInformation.getDeviceDetails(this)
            deviceDetails.tenantId = it.tenantId
            viewModel.switchOrganization(deviceDetails, it.tenantId)
        }
    }

    override fun onCancelButtonSelected() {
        val homeScreenFragment = supportFragmentManager.findFragmentByTag(TagHomeScreen)
        val privacyPolicyFragment = supportFragmentManager.findFragmentByTag(TagPrivacyPolicy)
        if(homeScreenFragment != null) {
            selectNavigationMenu(binding.navView.menu.findItem(R.id.home))
        } else if(privacyPolicyFragment != null) {
            selectNavigationMenu(binding.navView.menu.findItem(R.id.privacyPolicy))
        }
    }

    override fun onResume() {
        super.onResume()
        checkOfflineDataAvailability()
        doRefreshForDataUpdate()
    }

    private fun doRefreshForDataUpdate() {
        val role = SecuredPreference.getSelectedSiteEntity()?.role
        if (role == RoleConstant.PHYSICIAN_PRESCRIBER || role == RoleConstant.PROVIDER) {
            SecuredPreference.getSelectedSiteEntity()?.let { site ->
                viewModel.patientTransferNotificationCount(PatientTransferNotificationCountRequest(site.id))
            }
        }
    }

    private fun checkOfflineDataAvailability() {
        viewModel.checkOfflineDataCount()
    }

    private fun callSyncService() {
        if (connectivityManager.isNetworkAvailable()) {
            val startIntent = Intent(this, UploadForegroundService::class.java).apply {
                UploadForegroundService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(startIntent)
            } else {
                // Pre-O behavior.
                startService(startIntent)
            }
        }
    }

    private fun fetchUserMenuList(roleName: String) {
        viewModel.getUserMenuListByRole(roleName)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backHandelFlow()
        }
    }

    private fun backHandelFlow() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment: Fragment? = supportFragmentManager.findFragmentByTag(TagPrivacyPolicy)
            if (currentFragment is PrivacyPolicyFragment) {
                if (currentFragment.canGoBack()) {
                    currentFragment.goBack()
                } else {
                    onNavigationItemSelected(binding.navView.menu.findItem(R.id.home))
                }
            } else {
                finish()
            }
        }
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerOpened(drawerView: View) {
        when (drawerView.id) {
            R.id.nav_notification_view -> {
                SecuredPreference.getSelectedSiteEntity()?.let {
                    viewModel.getPatientListTransfer(PatientTransferNotificationCountRequest(it.id))
                }
            }
        }
    }

    override fun onDrawerClosed(drawerView: View) {
        when (drawerView.id) {
            R.id.nav_notification_view -> {
                doRefreshForDataUpdate()
            }
        }
    }

    override fun onDrawerStateChanged(newState: Int) {

    }

    override fun onTransferStatusUpdate(
        status: String,
        id: Long,
        tenantId: Long,
        reason: String
    ) {
        when (status) {
            TransferStatusEnum.REJECTED.name -> {
                showAlertDialogWithComments(
                    getString(R.string.reject),
                    message = getString(R.string.reject_confirmation),
                    getString(R.string.ok),
                    true,
                    errorMessage = getString(R.string.valid_reason)
                ) { isPositiveResult, rejectionReason ->
                    if (isPositiveResult) {
                        SecuredPreference.getSelectedSiteEntity()?.let {
                            viewModel.patientTransferUpdate(
                                PatientTransferUpdateRequest(
                                    id,
                                    it.tenantId,
                                    transferStatus = status,
                                    rejectReason = rejectionReason
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                SecuredPreference.getSelectedSiteEntity()?.let {
                    viewModel.patientTransferUpdate(
                        PatientTransferUpdateRequest(
                            id,
                            it.tenantId,
                            transferStatus = status
                        )
                    )
                }
            }
        }
    }

    override fun onViewDetail(patientID: Long) {
        PatientDetailDialogue.newInstance(patientID).show(supportFragmentManager,PatientDetailDialogue.TAG)
    }


}