package com.prm.tasksboard

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class OverviewActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var boardPagerAdapter: BoardPagerAdapter
    private lateinit var addBoardButton: Button
    private lateinit var menuButton: MaterialButton
    private lateinit var emptyView: TextView
    private val boardList = mutableListOf<BoardItem>()
    private val loggedInUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {

        checkFirestoreConnection()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_overview)
        setWindowInsetsListener()

        Log.d("FirebaseAuth", "Logged in user ID: $loggedInUserId")

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        addBoardButton = findViewById(R.id.addBoardButton)
        menuButton = findViewById(R.id.menuButton)
        emptyView = findViewById(R.id.emptyView)

        boardPagerAdapter = BoardPagerAdapter(boardList)
        viewPager.adapter = boardPagerAdapter

        // Link ViewPager2 and TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = boardList[position].name
        }.attach()

        setTabLayoutListeners()

        updateEmptyViewVisibility()

        addBoardButton.setOnClickListener {
            createNewBoard()
        }

        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, menuButton)
            if (boardList.isEmpty()) {
                popupMenu.menuInflater.inflate(
                    R.menu.board_options_menu_without_delete,
                    popupMenu.menu
                )
            } else {
                popupMenu.menuInflater.inflate(R.menu.board_options_menu, popupMenu.menu)
            }
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_board -> {
                        createNewBoard()
                        true
                    }

                    R.id.delete_board -> {
                        deleteBoard()
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun setWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setTabLayoutListeners() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabView = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(tab?.position ?: 0)
                tabView.setOnLongClickListener {
                    showRenameDialog(tab?.position ?: 0)
                    true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateEmptyViewVisibility() {
        emptyView.visibility = if (boardList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun createNewBoard() {
        // Create a new BoardItem
        val newBoard = BoardItem(
            createdAt = Timestamp.now(),
            name = "New Board #${boardList.size + 1}",
            updatedAt = Timestamp.now(),
            userId = loggedInUserId,
        )
        // Add it to your boardList
        boardList.add(newBoard)
        // Add it to Firestore
        addBoardItem(newBoard)
        // Notify the adapter that the dataset has changed
        boardPagerAdapter.notifyItemInserted(boardList.size - 1)
        // Refresh the TabLayout
        tabLayout.removeAllTabs()
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = boardList[pos].name
        }.attach()
        updateEmptyViewVisibility()
    }

    private fun deleteBoard() {
        // Delete the board from firestore
        deleteBoardItem(boardList[viewPager.currentItem].boardId)
        // Remove the board from your boardList
        boardList.removeAt(viewPager.currentItem)
        // Notify the adapter that the dataset has changed
        boardPagerAdapter.notifyItemRemoved(viewPager.currentItem)
        // If there are no boards left, detach the adapter
        if (boardList.isEmpty()) {
            viewPager.adapter = null
        }
        // Refresh the TabLayout
        tabLayout.removeAllTabs()
        if (boardList.isNotEmpty()) {
            TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
                tab.text = boardList[pos].name
            }.attach()
        }
        updateEmptyViewVisibility()
    }

    private fun showRenameDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Board")

        val input = EditText(this)
        input.setText(boardList[position].name)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newName = input.text.toString()
            boardList[position].name = newName
            tabLayout.getTabAt(position)?.text = newName
            boardPagerAdapter.notifyItemChanged(position)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun addBoardItem(boardItem: BoardItem) {
        // Firestore generates a unique ID for the new document
        val docRef = db.collection("boards").document()
        boardItem.boardId = docRef.id // Assign the Firestore-generated ID to the boardItem

        docRef.set(boardItem)
            .addOnSuccessListener {
                Log.d("FirestoreAdd", "BoardItem successfully added with ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreAdd", "Error adding BoardItem", e)
            }
    }

    private fun getBoardItems() {
        db.collection("boards")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("FirestoreRead", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreRead", "Error getting documents.", exception)
            }
    }

    private fun updateBoardItem(boardItemId: String, updatedFields: Map<String, Any>) {
        db.collection("boards").document(boardItemId)
            .update(updatedFields)
            .addOnSuccessListener {
                Log.d(
                    "FirestoreUpdate",
                    "Document $boardItemId successfully updated!"
                )
            }
            .addOnFailureListener { e -> Log.w("FirestoreUpdate", "Error updating document", e) }
    }

    private fun deleteBoardItem(boardItemId: String) {
        db.collection("boards").document(boardItemId)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    "FirestoreDelete",
                    "Document $boardItemId successfully deleted!"
                )
            }
            .addOnFailureListener { e -> Log.w("FirestoreDelete", "Error deleting document", e) }
    }

    fun checkFirestoreConnection() {
        db.collection("boards") // Replace "known_collection" with your actual collection name
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreCheck", "Connected to Firestore, but the collection is empty.")
                } else {
                    Log.d("FirestoreCheck", "Successfully connected to Firestore.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreCheck", "Error connecting to Firestore: ", exception)
            }
    }
}
