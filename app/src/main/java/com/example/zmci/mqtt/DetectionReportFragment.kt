package com.example.zmci.mqtt

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.zmci.R
import kotlinx.android.synthetic.main.fragment_detection_report.*
import org.json.JSONArray


class DetectionReportFragment : Fragment() {

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detection_report, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //get data from adapter position
        val image = arguments?.getString(DETECTION_IMAGE_KEY).toString()
        val cameraName = arguments?.getString(DETECTION_CAMERA_NAME_KEY).toString()
        val camera = arguments?.getString(DETECTION_CAMERA_KEY).toString()
        val timestamp = arguments?.getString(DETECTION_TIMESTAMP_KEY).toString()
        val violators = arguments?.getString(DETECTION_VIOLATORS_KEY).toString()
        val total_violations = arguments?.getString(TOTAL_VIOLATIONS_KEY).toString()
        val total_violators = arguments?.getString(TOTAL_VIOLATORS_KEY).toString()

        //decode base64 to image
        val decodedByte = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        imageReport.setImageBitmap(bitmap)

        //expandable image on click
        imageReport.setOnClickListener {
            zoomImageFromThumb(thumbView = imageReport,bitmap)
        }
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        //initialize text views
        cameraReport.text = "Camera details:"
        timestampReport.text = "Timestamp: $timestamp"
        violatorsReport.text = "Details:"
        totalViolationsReport.text = "Detected PPE: $total_violations"
        totalViolatorsReport.text = "Person: $total_violators"

        //Parse the JSON data
        try {
            // Convert the string to JSON
            val cameraObject = JSONArray("[ $camera ]")
            for (i in 0 until cameraObject.length()) {
                val itemCamera = cameraObject.getJSONObject(i)
                val camIP = itemCamera.getString("ip_address")
                // Create TextView for camera details
                val tvCameraDetails = TextView(context)
                // Setup attributes
                tvCameraDetails.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                tvCameraDetails.textSize = 20f
                tvCameraDetails.typeface = Typeface.DEFAULT_BOLD
                tvCameraDetails.text =
                    "Camera name: $cameraName\n" +
                            "IP: $camIP"
                // Display in LinearLayout
                cameraDetails.addView(tvCameraDetails)
            }
            // Convert the string to JSON
            val violatorsObject = JSONArray(violators)
            try {
                for (j in 0 until violatorsObject.length()) {
                    val itemViolators = violatorsObject.getJSONObject(j)
                    val personInfo = itemViolators.getString("person_info")
                    val personInfoObject = JSONArray(personInfo)

                    // If the current person has no 'face' and is not recognized,
                    // it is considered "Unknown person".
                    if (personInfoObject.length() == 0) {
                        // Create TextView for "Unknown person"
                        val tvPerson = TextView(context)
                        tvPerson.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                        tvPerson.textSize = 20f
                        tvPerson.typeface = Typeface.DEFAULT_BOLD
                        tvPerson.text = "Unknown person"
                        // Display the TextView in LinearLayout view
                        if (j % 2 == 0) {
                            detailsLinearLayout.addView(tvPerson)
                        } else {
                            detailsLinearLayout2.addView(tvPerson)
                        }
                    }

                    // If the current person has a detected 'face'...
                    for (k in 0 until personInfoObject.length()) {
                        val itemPILength = personInfoObject.getJSONObject(k).length()
                        // If the person has been recognized via face recognition
                        if (itemPILength > 1) {
                            val itemPI = personInfoObject.getJSONObject(k)
                            val personID = itemPI.getString("person_id")
                            val firstName = itemPI.getString("first_name")
                            val middleName = itemPI.getString("middle_name")
                            val lastName = itemPI.getString("last_name")
                            val jobTitle = itemPI.getString("job_title")
                            val overlaps = itemPI.getString("overlaps")
                            val tvPersonInfo = TextView(context)
                            tvPersonInfo.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                            tvPersonInfo.textSize = 20f
                            tvPersonInfo.typeface = Typeface.DEFAULT_BOLD
                            tvPersonInfo.text =
                                "ID: $personID\n" +
                                        "Name: $firstName $middleName $lastName\n" +
                                        "Job Title: $jobTitle\n" +
                                        "Overlaps: $overlaps"
                            if (j % 2 == 0) {
                                detailsLinearLayout.addView(tvPersonInfo)
                            } else {
                                detailsLinearLayout2.addView(tvPersonInfo)
                            }

                        }
                        // If the person is not recognized via 'face recognition'
                        // the person is considered "Unknown"
                        else {
                            val itemPI = personInfoObject.getJSONObject(j)
                            val overlaps = itemPI.getString("overlaps")
                            // Create TextView for "Unknown person"
                            val tvPersonUnknown = TextView(context)
                            tvPersonUnknown.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                            tvPersonUnknown.textSize = 20f
                            tvPersonUnknown.typeface = Typeface.DEFAULT_BOLD
                            tvPersonUnknown.text =
                                "Unknown person\n" +
                                        "Overlaps: $overlaps"
                            // Display the TextView in LinearLayout view
                            if (j % 2 == 0) {
                                detailsLinearLayout.addView(tvPersonUnknown)
                            } else {
                                detailsLinearLayout2.addView(tvPersonUnknown)
                            }
                        }

                    }

                    // Set the person's ID, and detections
                    val personUniqueID = itemViolators.getString("id")
                    val violations = itemViolators.getString("violations")
                    val violationsObject = JSONArray(violations)
                    // Create a TextView for person's ID and detections
                    val tvPersonID = TextView(context)
                    tvPersonID.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    tvPersonID.textSize = 20f
                    tvPersonID.typeface = Typeface.DEFAULT_BOLD
                    tvPersonID.text = "Person ID: $personUniqueID \n\nDetections:"
                    // Display the TextView in LinearLayout view
                    if (j % 2 == 0) {
                        detailsLinearLayout.addView(tvPersonID)
                    } else {
                        detailsLinearLayout2.addView(tvPersonID)
                    }
                    try {
                        // Processing of detected PPE with their corresponding color coding
                        for (l in 0 until violationsObject.length()) {
                            // Obtain the detections from JSON
                            val itemV = violationsObject.getJSONObject(l)
                            val className = itemV.getString("class_name")
                            // Create TextView for each detections
                            val tvPPE = TextView(context)
                            tvPPE.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                            tvPPE.textSize = 20f
                            tvPPE.typeface = Typeface.DEFAULT_BOLD
                            tvPPE.text = className
                            // Setting the color coding of each PPE detections
                            try {
                                when (className) {
                                    "helmet" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.helmet
                                            )
                                        )
                                    }

                                    "no helmet" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.no_helmet
                                            )
                                        )
                                    }

                                    "glasses" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.glasses
                                            )
                                        )
                                    }

                                    "no glasses" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.no_glasses
                                            )
                                        )
                                    }

                                    "vest" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.vest
                                            )
                                        )
                                    }

                                    "no vest" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.no_vest
                                            )
                                        )
                                    }

                                    "gloves" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.gloves
                                            )
                                        )
                                    }

                                    "no gloves" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.no_gloves
                                            )
                                        )
                                    }

                                    "boots" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.boots
                                            )
                                        )
                                    }

                                    "no boots" -> {
                                        tvPPE.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.no_boots
                                            )
                                        )
                                    }
                                }
                                // Display the TextView in LinearLayout view
                                if (j % 2 == 0) {
                                    detailsLinearLayout.addView(tvPPE)
                                } else {
                                    detailsLinearLayout2.addView(tvPPE)
                                }
                            } catch (e: Exception){
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // Horizontal line separator
                    val tvLine = TextView(context)
                    tvLine.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 10)
                    tvLine.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                    ))
                    // Display of Horizontal Line in LinearLayout view
                    if (j % 2 == 0) {
                        detailsLinearLayout.addView(tvLine)
                    } else {
                        detailsLinearLayout2.addView(tvLine)
                    }
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private fun zoomImageFromThumb(thumbView: View, imageResId: Bitmap) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        expandedImage.setImageBitmap(imageResId)

        // Calculate the starting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the
        // container view. Set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Using the "center crop" technique, adjust the start bounds to be the
        // same aspect ratio as the final bounds. This prevents unwanted
        // stretching during the animation. Calculate the start scaling factor.
        // The end scaling factor is always 1.0.
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally.
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically.
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it positions the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f

        animateZoomToLargeImage(startBounds, finalBounds, startScale)

        setDismissLargeImageAnimation(thumbView, startBounds, startScale)
    }

    private fun animateZoomToLargeImage(startBounds: RectF, finalBounds: RectF, startScale: Float) {
        expandedImage.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of the zoomed-in view. The default is the center of
        // the view.
        expandedImage.pivotX = 0f
        expandedImage.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties: X, Y, SCALE_X, and SCALE_Y.
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    expandedImage,
                    View.X,
                    startBounds.left,
                    finalBounds.left)
            ).apply {
                with(ObjectAnimator.ofFloat(expandedImage, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(expandedImage, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImage, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }
    }

    private fun setDismissLargeImageAnimation(thumbView: View, startBounds: RectF, startScale: Float) {
        // When the zoomed-in image is tapped, it zooms down to the original
        // bounds and shows the thumbnail instead of the expanded image.
        expandedImage.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning and sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImage, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImage, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImage, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(expandedImage, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImage.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImage.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }
}