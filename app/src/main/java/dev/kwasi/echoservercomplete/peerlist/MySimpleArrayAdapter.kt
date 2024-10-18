package dev.kwasi.echoservercomplete.peerlist

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dev.kwasi.echoservercomplete.R
import kotlin.math.log

class MySimpleArrayAdapter(private val students:List<Student> = listOf()): RecyclerView.Adapter<MySimpleArrayAdapter.ViewHolder>() {
    private val studentIds:MutableList<Student> = students.toMutableList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.studentIDText)
        val descriptionTextView: TextView = itemView.findViewById(R.id.StudentNameText)
//        val connectButtonView: Button = itemView.findViewById(R.id.StudentButtonID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MySimpleArrayAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MySimpleArrayAdapter.ViewHolder, position: Int) {
        val ids = studentIds[position]
        holder.titleTextView.text = ids.id
        holder.descriptionTextView.text = ids.name
    }

    override fun getItemCount(): Int {
        return students.size
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun updateList(){
//        peersList.clear()
//        peersList.addAll(newPeersList)
//        notifyDataSetChanged()
//    }
}