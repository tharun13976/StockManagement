package com.example.stockmanagement

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class CustomFilterArrayAdapter(
    context: Context,
    private val allItems: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, ArrayList()) {

    private val filteredItems = ArrayList<String>()

    override fun getCount(): Int = filteredItems.size

    override fun getItem(position: Int): String? = filteredItems[position]

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                filteredItems.clear()

                if (constraint != null) {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in allItems) {
                        if (item.lowercase().contains(filterPattern)) {
                            filteredItems.add(item)
                        }
                    }
                } else {
                    filteredItems.addAll(allItems)
                }

                filterResults.values = filteredItems
                filterResults.count = filteredItems.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue as String
            }
        }
    }
}
