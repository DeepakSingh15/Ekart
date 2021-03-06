package com.example.e_kart.activity.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.e_kart.R
import com.example.e_kart.activity.activities.AddEditAddressActivity
import com.example.e_kart.activity.activities.CheckoutActivity
import com.example.e_kart.models.Address
import com.example.e_kart.utils.Constants
import kotlinx.android.synthetic.main.item_address_layout.view.*

open class AddressListAdapter(private val context: Context, private var list: ArrayList<Address>,private var selectAddress:Boolean) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyProductsListAdapter.MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_address_layout, parent, false)
        )
    }

    fun notifyEditItem(activity: Activity, position:Int){
        val intent = Intent(context,AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, list[position])
        activity.startActivityForResult(intent,Constants.ADD_ADDRESS_REQUEST_CODE)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyProductsListAdapter.MyViewHolder) {
            holder.itemView.tv_address_full_name.text = model.name
            holder.itemView.tv_address_type.text = model.type
            holder.itemView.tv_address_details.text = "${model.address}, ${model.zipCode}"
            holder.itemView.tv_address_mobile_number.text = model.mobileNumber

            if (selectAddress){
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, CheckoutActivity::class.java)
                    intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS,model)
                    context.startActivity(intent)
                }
            }
        }
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}