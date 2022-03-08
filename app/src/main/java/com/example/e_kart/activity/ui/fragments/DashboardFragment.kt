package com.example.e_kart.activity.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.e_kart.R
import com.example.e_kart.activity.activities.CartListActivity
import com.example.e_kart.activity.activities.SettingActivity
import com.example.e_kart.activity.adapters.DashboardItemsListAdapter
import com.example.e_kart.firestore.FirestoreClass
import com.example.e_kart.models.Product
import kotlinx.android.synthetic.main.fragment_dashboard.*


class DashboardFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If we want to use the option menu in fragment we need to add it.
        setHasOptionsMenu(true)
    }

    fun successDashboardItemsList(dashboardItemsList: ArrayList<Product>){
        hideProgressDialog()

        for (i in dashboardItemsList){
            if (dashboardItemsList.size > 0){
                rv_dashboard_items.visibility = View.VISIBLE
                tv_no_dashboard_items_found.visibility = View.GONE

                rv_dashboard_items.layoutManager = GridLayoutManager(activity,2)
                rv_dashboard_items.setHasFixedSize(true)
                val adapter = DashboardItemsListAdapter(requireActivity(),dashboardItemsList)
                rv_dashboard_items.adapter = adapter
            }else{
                rv_dashboard_items.visibility = View.GONE
                tv_no_dashboard_items_found.visibility = View.VISIBLE
            }
        }

    }

    private fun getDashboardItemsList(){
        showProgressDialog()

        FirestoreClass().getDashboardItemsList(this@DashboardFragment)
    }

    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)


        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {

            R.id.action_settings -> {

                startActivity(Intent(activity, SettingActivity::class.java))
                return true
            }
            R.id.action_cart -> {
                startActivity(Intent(activity, CartListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}