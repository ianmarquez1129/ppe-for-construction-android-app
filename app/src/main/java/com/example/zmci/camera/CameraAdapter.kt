package com.example.zmci.camera

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.example.zmci.database.DatabaseHelper
import kotlinx.android.synthetic.main.fragment_add_camera.view.*
import kotlinx.android.synthetic.main.fragment_update_camera.view.*

class CameraAdapter(val c:Context, val cameraList:MutableList<CameraData>):RecyclerView.Adapter<CameraAdapter.CameraViewHolder>() {

    inner class CameraViewHolder(val v:View,listener: onItemClickListener):RecyclerView.ViewHolder(v){
        var name:TextView = v.findViewById(R.id.mTitle)
        var mMenus:ImageView = v.findViewById(R.id.mMenus)
        private lateinit var databaseHelper: DatabaseHelper

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
                        val v = LayoutInflater.from(c).inflate(R.layout.fragment_update_camera,null)
                        val updateCameraName = v.findViewById<EditText>(R.id.cameraNameUpdate)

                        val newList = cameraList[adapterPosition]
                        position.cameraName = newList.cameraName


                        AlertDialog.Builder(c)
                            .setView(v)
                            .setPositiveButton("Update"){
                                dialog,_->
                                val isUpdate = CameraFragment.databaseHelper.updateCamera(
                                    newList.id.toString(),
                                    v.cameraNameUpdate.text.toString())
                                if (isUpdate) {
                                    cameraList[adapterPosition].cameraName = v.cameraNameUpdate.text.toString()
                                    notifyDataSetChanged()
                                    Toast.makeText(c,"Updated Successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(c, "Error Updating", Toast.LENGTH_SHORT).show()
                                }
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
                        val newList = cameraList[adapterPosition]
                        position.cameraName = newList.cameraName
                        val cameraName = newList.cameraName

                        AlertDialog.Builder(c)
                            .setTitle("Warning")
                            .setMessage("Are you sure you want to delete : $cameraName ?")
                            .setPositiveButton("Yes", DialogInterface.OnClickListener{ dialog, which ->
                                if (CameraFragment.databaseHelper.deleteCamera(newList.id)) {
                                    cameraList.removeAt(adapterPosition)
                                    notifyItemRemoved(adapterPosition)
                                    notifyItemRangeChanged(adapterPosition,cameraList.size)
                                    Toast.makeText(c, "Camera $cameraName Deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(c, "Error Deleting", Toast.LENGTH_SHORT).show()
                                }
                            })
                            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->  })
                            .setIcon(R.drawable.ic_warning)
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