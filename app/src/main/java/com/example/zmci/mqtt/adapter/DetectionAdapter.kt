package com.example.zmci.mqtt.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.example.zmci.mqtt.DetectionFragment
import com.example.zmci.mqtt.model.Detection

class DetectionAdapter(val c: Context, val detectionList:MutableList<Detection>): RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    inner class DetectionViewHolder(val v: View, listener: onItemClickListener): RecyclerView.ViewHolder(v){
        val rCameraName: TextView = v.findViewById(R.id.rCameraName)
        var rTimestamp: TextView = v.findViewById(R.id.rTimestamp)
        var rViolators: TextView = v.findViewById(R.id.rViolators)
        var rViolations: TextView = v.findViewById(R.id.rViolations)
        var rMenus: ImageView = v.findViewById(R.id.rMenus)

        init {
            rMenus.setOnClickListener{ popupMenus(it) }
            v.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

        private fun popupMenus(v: View) {
            val position = detectionList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.reports_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.deleteReport->{
                        val newList = detectionList[adapterPosition]
                        position.image = newList.image
                        position.cameraName = newList.cameraName
                        position.camera = newList.camera
                        position.timestamp = newList.timestamp
                        position.violators = newList.violators
                        position.total_violations = newList.total_violations
                        position.total_violators = newList.total_violators

                        val timestamp = newList.timestamp

                        AlertDialog.Builder(c)
                            .setTitle("Warning")
                            .setMessage("Are you sure you want to delete: $timestamp ?")
                            .setPositiveButton("Yes", DialogInterface.OnClickListener{ dialog, which ->
                                if (DetectionFragment.databaseHelper.deleteDetection(newList.id)) {
                                    detectionList.removeAt(adapterPosition)
                                    notifyItemRemoved(adapterPosition)
                                    notifyItemRangeChanged(adapterPosition,detectionList.size)
                                    Toast.makeText(c, "Record $timestamp Deleted", Toast.LENGTH_SHORT).show()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.fragment_list_reports,parent,false)
        return DetectionViewHolder(v,rListener)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        val newList = detectionList[position]
        holder.rCameraName.text = newList.cameraName
        holder.rTimestamp.text = "Timestamp: ${newList.timestamp}"
        holder.rViolators.text = "Person: ${newList.total_violators}"
        holder.rViolations.text = "Detected PPE: ${newList.total_violations}"
    }

    override fun getItemCount(): Int {
        return detectionList.size
    }

    private lateinit var rListener : onItemClickListener

    interface onItemClickListener{

        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        rListener = listener
    }


}