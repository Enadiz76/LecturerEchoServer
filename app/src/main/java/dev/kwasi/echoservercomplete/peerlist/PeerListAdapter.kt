package dev.kwasi.echoservercomplete.peerlist

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.kwasi.echoservercomplete.R

class PeerListAdapter(private val iFaceImpl:PeerListAdapterInterface): RecyclerView.Adapter<PeerListAdapter.ViewHolder>() {
    private val peersList:MutableList<WifiP2pDevice> = mutableListOf()
    private val studentIds:MutableList<Student> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
//        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val connectButtonView: Button = itemView.findViewById(R.id.StudentButtonID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.peer_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < peersList.size && position < studentIds.size) {
            val peer = peersList[position]
            val ids = studentIds[position]

            holder.titleTextView.text = peer.deviceName
            holder.connectButtonView.text = peer.deviceAddress

            holder.itemView.setOnClickListener {
                iFaceImpl.onPeerClicked(peer)
            }
        }
    }

    override fun getItemCount(): Int {
        return peersList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newPeersList:Collection<WifiP2pDevice>){
        peersList.clear()
        peersList.addAll(newPeersList)
        notifyDataSetChanged()
    }
}