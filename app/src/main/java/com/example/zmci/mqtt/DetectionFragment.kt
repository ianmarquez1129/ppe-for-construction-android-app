package com.example.zmci.mqtt

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zmci.R
import com.example.zmci.database.DatabaseHelper
import com.example.zmci.mqtt.adapter.DetectionAdapter
import com.example.zmci.mqtt.model.Detection
import kotlinx.android.synthetic.main.fragment_detection.*
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.Executors

class DetectionFragment : Fragment() {

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }
    private lateinit var detectionAdapter: DetectionAdapter

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this.requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set List
        val detectionList = databaseHelper.getAllDetection(requireContext())
        // set adapter
        detectionList.sortByDescending { it.id }
        detectionAdapter = DetectionAdapter(requireContext(), detectionList)
        //set find Id
        val rvReports: RecyclerView = rvDetection
        //set recycler view adapter
        rvReports.layoutManager = LinearLayoutManager(this.context)
        rvReports.adapter = detectionAdapter

        var adapter = detectionAdapter
        rvReports.adapter = adapter
        adapter.setOnItemClickListener(object : DetectionAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val image             = detectionList[position].image
                val cameraName        = detectionList[position].cameraName
                val camera            = detectionList[position].camera
                val timestamp         = detectionList[position].timestamp
                val violators         = detectionList[position].violators
                val total_violations  = detectionList[position].total_violations
                val total_violators   = detectionList[position].total_violators

                val detectionBundle = bundleOf(
                    DETECTION_IMAGE_KEY      to image,
                    DETECTION_CAMERA_NAME_KEY to cameraName,
                    DETECTION_CAMERA_KEY     to camera,
                    DETECTION_TIMESTAMP_KEY  to timestamp,
                    DETECTION_VIOLATORS_KEY  to violators,
                    TOTAL_VIOLATIONS_KEY to total_violations,
                    TOTAL_VIOLATORS_KEY to total_violators)
                findNavController().navigate(
                    R.id.action_DetectionFragment_to_DetectionReportFragment, detectionBundle)
            }
        })
        //progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        //set dialog
        btnDeleteAll.setOnClickListener { deleteAllDetection() }
        btnPDF.setOnClickListener { exportToPDF() }
    }

    private fun deleteAllDetection() {
        val deleteDialog = AlertDialog.Builder(this.requireContext())

        deleteDialog.setTitle("Warning")
        deleteDialog.setMessage("Are you sure you want to permanently delete all records?")
        deleteDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
            if (databaseHelper.deleteAllDetection()) {
                detectionAdapter.notifyDataSetChanged()
                Toast.makeText(this.requireContext(), "All Records are Deleted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this.requireContext(), "Error Deleting", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        })
        deleteDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        deleteDialog.create()
        deleteDialog.show()
    }

    private fun exportToPDF() {
        val exportDialog = AlertDialog.Builder(this.requireContext())

        exportDialog.setTitle("Export to PDF")
        exportDialog.setMessage("Are you sure you want to export all data to PDF?")
        exportDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
            convertDataToPDF()
            //Export data to PDF
            dialog.dismiss()
        })
        exportDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        exportDialog.create()
        exportDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertDataToPDF() {

        //set progress dialog message
        progressDialog.setMessage("Exporting to PDF...")
        progressDialog.show()

        //init ExecutorService for background processing
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executorService.execute {
            // convert data to pdf in background process

            try {
                // Set the file extension of PDF
                val currentTime = System.currentTimeMillis()
                val simpleDateFormat = SimpleDateFormat("MMM-dd-yyyy_HH-mm")
                val timestamp = simpleDateFormat.format(currentTime)


                // create page description initial values
                lateinit var pdfDocument : PdfDocument
                lateinit var pageInfo: PageInfo
                lateinit var page: PdfDocument.Page
                lateinit var canvas : Canvas
                lateinit var paint : Paint
                val pageWidth = 595 //A4
                val pageHeight = 842 //A4
                val marginLeft = 8F
                val textSize = 16F
                var y = textSize
                var currentPage = 1

                fun titleHeader() {
                    val titlePaint = Paint()
                    titlePaint.textAlign = Paint.Align.CENTER
                    titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    titlePaint.textSize = 20F
                    canvas.drawText("Records as of $timestamp", pageWidth.toFloat()/2, y+marginLeft, titlePaint)
                    y += 20F
                }

                fun initPdfDocument() {
                    //create new PDF Document
                    pdfDocument = PdfDocument()
                    // create page description
                    pageInfo = PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                    // start a page
                    page = pdfDocument.startPage(pageInfo)
                    // draw something on the page
                    canvas = page.canvas
                    // for page default configuration
                    paint = Paint()
                    // configurations
                    paint.color = Color.BLACK
                    paint.textSize = textSize
                }

                fun writeToPdfDocument(strCameraName: String, strTimestamp: String, strPerson: String, strDetection: String) {

                    if (y > pageHeight) {
                        pdfDocument.finishPage(page)
                        currentPage++
                        pageInfo = PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = textSize
                    }

                    val separator = pageWidth/4
                    canvas.drawText(strCameraName, marginLeft, y, paint)
                    canvas.drawText(strTimestamp, marginLeft+separator, y, paint)
                    canvas.drawText(strPerson, marginLeft+separator* 2.5F, y, paint)
                    canvas.drawText(strDetection, marginLeft+separator*3, y, paint)
                    y += textSize

                }

                fun closePdfDocument() {
                    // finish the page
                    pdfDocument.finishPage(page)
                }

                fun savePdfDocument() {
                    // Create folder where the PDF files will be saved
                    val root = File(requireContext().getExternalFilesDir(null), Constants.PDF_FOLDER)
                    root.mkdirs()
                    val fileName = "PDF_$timestamp.pdf"
                    val file = File(root, fileName)
                    val fileOutputStream = FileOutputStream(file)
                    // Add PDF pages to PDF Document
                    pdfDocument.writeTo(fileOutputStream)
                    pdfDocument.close()
                }

                fun createAndWritePdf() {
                    initPdfDocument()
                    titleHeader()
                    writeToPdfDocument(" "," ", " ", " ")
                    writeToPdfDocument("Camera Name","Timestamp","Person","Detection")
                    writeToPdfDocument(" "," ", " ", " ")

                    //get data in database
                    val detectionList = databaseHelper.getAllDetection(requireContext())
                    val dataList:MutableList<Detection> = detectionList
                    for (i in dataList.indices){
                        // data that will fill the column
                        val cameraNameColumn = dataList[i].cameraName
                        val timestampColumn = dataList[i].timestamp
                        val totalPersonColumn = dataList[i].total_violators
                        val totalDetectionColumn = dataList[i].total_violations

                        try {
                            // fill the column with the data
                            writeToPdfDocument(cameraNameColumn,timestampColumn,totalPersonColumn,totalDetectionColumn)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    writeToPdfDocument("**nothing follows**","","","")
                    closePdfDocument()
                    savePdfDocument()
                }

                // initiate all tasks
                createAndWritePdf()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            handler.post {
                progressDialog.dismiss()
                Toast.makeText(
                    this.requireContext(),
                    "Records exported successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }


    }

}