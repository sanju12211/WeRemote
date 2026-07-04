package com.weremote.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persists the user's saved remotes locally (SharedPreferences + JSON). */
class RemoteStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("we_remote", Context.MODE_PRIVATE)

    fun all(): List<SavedRemote> {
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = ArrayList<SavedRemote>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                SavedRemote(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    typeId = o.getString("typeId"),
                    brandName = o.getString("brandName"),
                    codeset = o.optString("codeset", "")
                )
            )
        }
        return out
    }

    fun add(remote: SavedRemote) {
        val list = all().toMutableList()
        list.add(remote)
        save(list)
    }

    fun remove(id: String) {
        save(all().filterNot { it.id == id })
    }

    private fun save(list: List<SavedRemote>) {
        val arr = JSONArray()
        for (r in list) {
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("name", r.name)
                    .put("typeId", r.typeId)
                    .put("brandName", r.brandName)
                    .put("codeset", r.codeset)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    companion object {
        private const val KEY = "remotes"
    }
}
