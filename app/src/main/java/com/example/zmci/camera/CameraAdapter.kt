package com.example.zmci.camera

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R

class CameraAdapter(val c:Context, val cameraList:ArrayList<CameraData>):RecyclerView.Adapter<CameraAdapter.CameraViewHolder>() {

    inner class CameraViewHolder(val v:View,listener: onItemClickListener):RecyclerView.ViewHolder(v){
        var name:TextView = v.findViewById(R.id.mTitle)
        var status:TextView = v.findViewById(R.id.mSubtitle)
        var mMenus:ImageView = v.findViewById(R.id.mMenus)

        init {
            mMenus.setOnClickListener{ popupMenus(it) }
            v.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

        private fun popupMenus(v:View) {
            val position = cameraList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val v = LayoutInflater.from(c).inflate(R.layout.fragment_add_camera,null)
                        val name = v.findViewById<EditText>(R.id.cameraName)
                        val notification = v.findViewById<SwitchCompat>(R.id.swNotification)
                        val identifiers = v.findViewById<SwitchCompat>(R.id.swIdentifiers)
                        val helmet = v.findViewById<SwitchCompat>(R.id.swHelmet)
                        val glasses = v.findViewById<SwitchCompat>(R.id.swGlasses)
                        val vest = v.findViewById<SwitchCompat>(R.id.swVest)
                        val gloves = v.findViewById<SwitchCompat>(R.id.swGloves)
                        val boots = v.findViewById<SwitchCompat>(R.id.swBoots)

                        AlertDialog.Builder(c)
                            .setView(v)
                            .setPositiveButton("Ok"){
                                dialog,_->
                                position.cameraName = name.text.toString()
                                position.notification = notification.isChecked
                                position.identifiers = identifiers.isChecked
                                position.helmet = helmet.isChecked
                                position.glasses = glasses.isChecked
                                position.vest = vest.isChecked
                                position.gloves = gloves.isChecked
                                position.boots = boots.isChecked
                                notifyDataSetChanged()
                                Toast.makeText(c,"Camera is Edited",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel"){
                                dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    R.id.delete->{
                        AlertDialog.Builder(c)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("Are you sure you want to delete?")
                            .setPositiveButton("Yes"){
                                dialog,_->
                                cameraList.removeAt(adapterPosition)
                                notifyDataSetChanged()
                                Toast.makeText(c,"Camera is Deleted",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No"){
                                dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    else-> true
                }
            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CameraViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.fragment_list_camera,parent,false)
        return CameraViewHolder(v,mListener)
    }

    override fun onBindViewHolder(holder: CameraViewHolder, position: Int) {
        val newList = cameraList[position]
        holder.name.text = newList.cameraName
        holder.status.text = newList.cameraStatus
    }

    override fun getItemCount(): Int {
        return cameraList.size
    }

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{

        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }


}