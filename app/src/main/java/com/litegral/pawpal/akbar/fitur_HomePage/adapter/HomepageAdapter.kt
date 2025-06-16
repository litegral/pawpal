package com.litegral.pawpal.akbar.fitur_HomePage.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.litegral.pawpal.R
import com.litegral.pawpal.akbar.model.CatModel

class HomepageAdapter(
    private var petList: List<CatModel>,
    private val onItemClicked: (CatModel) -> Unit
) : RecyclerView.Adapter<HomepageAdapter.PetViewHolder>() {

    class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageCat: ImageView = view.findViewById(R.id.imageCat)
        private val textName: TextView = view.findViewById(R.id.textName)
        private val textAge: TextView = view.findViewById(R.id.textAge)
        private val textLocation: TextView = view.findViewById(R.id.textLocation)
        private val textGender: TextView = view.findViewById(R.id.textGender)

        // FUNCTION PEMASUKAN DATA KE DALAM UI
        fun bind(pet: CatModel, onItemClicked: (CatModel) -> Unit) {
            textName.text = pet.name
            textAge.text = pet.age
            textLocation.text = pet.petPosition



            if (pet.imageUrls.isNotEmpty()) {
                // Ambil URL gambar pertama (indeks 0) sebagai gambar utama/profil
                val mainImageUrl = pet.imageUrls[0]


                Glide.with(itemView.context)
                    .load(mainImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(imageCat)
            } else {
               // PLACEHOLDER JIKA GAMBAR TIDAK ADA
                imageCat.setImageResource(R.drawable.ic_profile_placeholder)
            }


            // LOGIKA MENAMPILKAN GENDER JIKA MALE ATAU FEMALE
            if (pet.isFemale) {
                textGender.text = "♀"
                textGender.setTextColor(Color.parseColor("#F200FF")) // Warna pink
            } else {
                textGender.text = "♂"
                textGender.setTextColor(Color.parseColor("#FCA93F")) // Warna kuning/oranye
            }

            // SEMUA ITEM DI PENCET AKAN MEMBUAT BERPINDAH KE FRAGMENT UNTUK MELIHAT DETAIL HEWAN
            itemView.setOnClickListener {
                onItemClicked(pet)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cat, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(petList[position], onItemClicked)
    }

    override fun getItemCount(): Int = petList.size

    fun updateData(newList: List<CatModel>) {
        petList = newList
        notifyDataSetChanged()
    }
}