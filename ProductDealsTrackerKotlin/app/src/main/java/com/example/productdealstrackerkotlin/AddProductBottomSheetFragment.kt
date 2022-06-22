package com.example.productdealstrackerkotlin
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_tracking_details_layout.*

class AddProductBottomSheetFragment : BottomSheetDialogFragment(){

    private lateinit var mListener:BottomSheetListener
    lateinit var activityResultLauncher : ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.add_tracking_details_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openBrowserFlipkart.setOnClickListener {

            val intent = Intent(view.context,WebViewActivityFlipkart::class.java)
            activityResultLauncher.launch(intent)

        }

        openBrowserAmazon.setOnClickListener {

            val intent = Intent(view.context,WebViewActivityAmazon::class.java)
            activityResultLauncher.launch(intent)

        }

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult? ->

            if(result!!.resultCode == Activity.RESULT_OK){

                print("GOT RESULT BACK")
                var chosenUrl = result.data?.extras?.getString("url")
                url_input.setText(chosenUrl)

            }

        }

        addItemButton.setOnClickListener {
            
            var url = url_input.text
            val budget = budget_value.text.toString()
            var trackingInput = spinner2.selectedItem.toString()
            
            if(url.isNullOrEmpty() or budget.isNullOrEmpty()){
                Toast.makeText(view.context, "Input Cannot Be Empty", Toast.LENGTH_SHORT).show()
            }
            else{
                mListener.onAddItemButtonClicked(url.toString(), budget.toInt(),trackingInput)
            }

            dismiss()
            url_input.text?.clear()
            budget_value.text?.clear()
        }
    }

    public interface BottomSheetListener{
        fun onAddItemButtonClicked(url_input: String, budget: Int,trackInterval : String);
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is BottomSheetListener) {
            mListener = context
        }
        else{
            throw RuntimeException(requireContext().toString() + " must implement BottomSheetListener")
        }
    }
}