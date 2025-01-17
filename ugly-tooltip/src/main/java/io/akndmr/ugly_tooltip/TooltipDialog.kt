package io.akndmr.ugly_tooltip

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ScrollView
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 * Created by Akın DEMİR on 2.06.2021.
 * Copyright (c) 2021
 */


class TooltipDialog : DialogFragment() {

    val DELAY_SCROLLING = 350
    val LOG_TAG: String = TooltipDialog::class.java.simpleName
    val MAX_RETRY_LAYOUT = 3

    private var tutorsList: ArrayList<TooltipObject>? = null
    private var currentTutorIndex = -1
    private var builder: TooltipBuilder? = null
    private var dialogTag: String? = null

    var hasViewGroupHandled = false
    private var mFragmentManager: FragmentManager? = null

    private var retryCounter = 0

    private var nextListener: TooltipDialogListener.NextListener? = null
    private var previousListener: TooltipDialogListener.PreviousListener? = null
    private var completeListener: TooltipDialogListener.CompleteListener? = null
    private var skipListener: TooltipDialogListener.SkipListener? = null

    private var nextListenerShouldUseInstanceFunc = true
    private var prevListenerShouldUseInstanceFunc = true
    private var completeListenerShouldUseInstanceFunc = true
    private var skipListenerShouldUseInstanceFunc = true

    companion object {
        private val ARG_BUILDER = "BUILDER"

        fun newInstance(builder: TooltipBuilder): TooltipDialog {
            val args = Bundle()
            val fragment = TooltipDialog()
            args.putParcelable(ARG_BUILDER, builder)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getArgs(arguments)
    }

    private fun getArgs(args: Bundle?) {
        builder = args!![ARG_BUILDER] as TooltipBuilder?
    }

    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog =
            object : Dialog(requireContext(), io.akndmr.ugly_tooltip.R.style.UglyTooltip) {
                override
                fun onBackPressed() {
                    if (builder != null) {
                        if (builder!!.isClickable()) {
                            previous()
                        }
                    }
                }
            }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = TooltipLayout(requireActivity(), builder)
        initViews(view as TooltipLayout)
        return view
    }

    private fun initViews(view: TooltipLayout) {
        view.setTooltipListener(object : TooltipListener {
            override
            fun onPrevious() {
                if (previousListener != null && !prevListenerShouldUseInstanceFunc) {
                    return previousListener!!.onPrevious(currentTutorIndex, tutorsList!![currentTutorIndex])
                }

                previous()
            }

            override
            fun onNext() {
                if (nextListener != null && !nextListenerShouldUseInstanceFunc) {
                    return nextListener!!.onNext(currentTutorIndex, tutorsList!![currentTutorIndex])
                }

                next()
            }

            override
            fun onComplete() {
                if (completeListener != null && tutorsList != null) {
                    if (completeListenerShouldUseInstanceFunc) {
                        completeListener!!.onComplete(tutorsList!![currentTutorIndex])
                    } else {
                        return completeListener!!.onComplete(tutorsList!![currentTutorIndex])
                    }
                }

                if (!TextUtils.isEmpty(dialogTag)) {
                    TooltipPreference.setShown(requireContext(), dialogTag, true)
                }

                this@TooltipDialog.close()
            }
        })

        view.setSkipListener(object : TooltipListener.SkipListener {
            override fun onSkip() {
                if (skipListener != null && tutorsList != null) {
                    if (skipListenerShouldUseInstanceFunc) {
                        skipListener!!.onSkip(currentTutorIndex, tutorsList!![currentTutorIndex])
                    } else {
                        return skipListener!!.onSkip(currentTutorIndex, tutorsList!![currentTutorIndex])
                    }

                }

                this@TooltipDialog.close()
            }
        })

        if (builder != null) {
            isCancelable = builder!!.isClickable()
        }
    }

    operator fun next() {
        if (currentTutorIndex + 1 >= (tutorsList?.size ?: 0)) {
            close()
        } else {
            if (tutorsList != null) {
                this@TooltipDialog.show(
                    activity,
                    mFragmentManager!!,
                    dialogTag,
                    tutorsList!!,
                    currentTutorIndex + 1
                )

                nextListener?.onNext(currentTutorIndex, tutorsList!![currentTutorIndex])
            }
        }
    }

    fun previous() {
        if (currentTutorIndex - 1 < 0) {
            currentTutorIndex = 0
        } else {
            if (tutorsList != null) {
                this@TooltipDialog.show(
                    activity,
                    mFragmentManager!!,
                    dialogTag,
                    tutorsList!!,
                    currentTutorIndex - 1
                )

                previousListener?.onPrevious(currentTutorIndex, tutorsList!![currentTutorIndex])
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val window: Window? = dialog!!.window
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.transparent)
            window.setDimAmount(0f)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun hasShown(context: Context, tag: String): Boolean {
        return TooltipPreference.hasShown(context, tag)
    }


    fun show(
        activity: Activity?,
        fm: FragmentManager,
        sharedPrefTag: String? = null,
        tutorList: ArrayList<TooltipObject>
    ) {
        mFragmentManager = fm
        show(activity, fm, sharedPrefTag, tutorList, 0)
    }

    fun showWithCallback(
        activity: Activity?,
        fm: FragmentManager,
        sharedPrefTag: String? = null,
        tutorList: ArrayList<TooltipObject>,
        onStep: (Int) -> Unit
    ) {
        mFragmentManager = fm
        show(activity, fm, sharedPrefTag, tutorList, 0, onStep)
    }

    private fun show(
        activity: Activity?,
        fm: FragmentManager,
        sharedPrefTag: String? = null,
        tutorList: ArrayList<TooltipObject>,
        index: Int,
        onStep: ((Int) -> Unit)? = null
    ) {
        if (activity == null || activity.isFinishing) {
            return
        }

        var indexToShow = index

        try {
            tutorsList = tutorList
            this.dialogTag = sharedPrefTag

            if (indexToShow < 0 || indexToShow >= tutorList.size) {
                indexToShow = 0
            }

            val previousIndex = currentTutorIndex
            currentTutorIndex = indexToShow
            hasViewGroupHandled = false

            onStep?.invoke(currentTutorIndex)

            if (currentTutorIndex == tutorList.lastIndex + 1) {
                hasViewGroupHandled = true
            }


            // has been handled by listener
            if (hasViewGroupHandled) {
                return
            }

            val tooltipObject: TooltipObject = tutorList[currentTutorIndex]
            val viewGroup: ViewGroup? = tooltipObject.scrollView
            if (viewGroup != null) {
                val viewToFocus: View? = tooltipObject.view
                hasViewGroupHandled = if (viewToFocus != null) {
                    hideLayout()
                    viewGroup.post {
                        if (viewGroup is ScrollView) {
                            val scrollView = viewGroup
                            val relativeLocation = IntArray(2)
                            TooltipViewHelper.getRelativePositionRec(
                                viewToFocus,
                                viewGroup,
                                relativeLocation
                            )
                            scrollView.smoothScrollTo(0, relativeLocation[1])
                            scrollView.postDelayed(
                                {
                                    showLayout(activity, fm, tooltipObject)
                                },
                                DELAY_SCROLLING.toLong()
                            )
                        } else if (viewGroup is NestedScrollView) {
                            val scrollView = viewGroup
                            val relativeLocation = IntArray(2)
                            TooltipViewHelper.getRelativePositionRec(
                                viewToFocus,
                                viewGroup,
                                relativeLocation
                            )
                            scrollView.smoothScrollTo(0, relativeLocation[1])
                            scrollView.postDelayed(
                                {
                                    showLayout(activity, fm, tooltipObject)
                                },
                                DELAY_SCROLLING.toLong()
                            )
                        }
                    }
                    true
                } else {
                    false
                }
            }
            if (!hasViewGroupHandled) {
                showLayout(activity, fm, tutorsList!![currentTutorIndex])
            }
        } catch (e: Exception) {
            // to Handle the unknown exception.
            // Since this only for first guide, if any error appears, just don't show the guide
            Log.e(LOG_TAG, e.stackTraceToString())
            try {
                this@TooltipDialog.dismiss()
            } catch (e2: Exception) {
                // no op
                Log.e(LOG_TAG, e2.stackTraceToString())
            }
        }
    }

    fun showLayout(activity: Activity?, fm: FragmentManager?, tooltipObject: TooltipObject) {
        if (activity == null || activity.isFinishing) {
            return
        }

        //val fm: FragmentManager? = childFragmentManager
        if (!isVisible) {
            try {
                if (fm != null) {
                    if (!isAdded) {
                        show(fm, LOG_TAG)
                    } else if (isHidden) {
                        val ft: FragmentTransaction = fm.beginTransaction()
                        ft.show(this@TooltipDialog)
                        ft.commit()
                    }
                }
            } catch (e: IllegalStateException) {
                // called in illegal state. just return.
                return
            }
        }
        val view: View? = tooltipObject.view
        val title: String? = tooltipObject.title
        val text: String? = tooltipObject.text
        val tooltipContentPosition: TooltipContentPosition =
            tooltipObject.tooltipContentPosition
        val tintBackgroundColor: Int = tooltipObject.tintBackgroundColor
        val location: IntArray? = tooltipObject.getLocation()
        val radius: Int = tooltipObject.getRadius()
        if (view == null) {
            layoutShowTutorial(
                null, title, text, tooltipContentPosition,
                tintBackgroundColor, location, radius
            )
        } else {
            view.post(Runnable {
                layoutShowTutorial(
                    view, title, text, tooltipContentPosition,
                    tintBackgroundColor, location, radius
                )
            })
        }
    }

    fun hideLayout() {
        val layout: TooltipLayout = this@TooltipDialog.view as TooltipLayout ?: return
        layout.hideTutorial()
    }

    private fun layoutShowTutorial(
        view: View?,
        title: String?,
        text: String?,
        showCaseContentPosition: TooltipContentPosition,
        tintBackgroundColor: Int,
        customTarget: IntArray?,
        radius: Int
    ) {
        try {
            val layout: TooltipLayout = this@TooltipDialog.view as TooltipLayout
            retryCounter = 0
            layout.showTutorial(
                view, title, text, currentTutorIndex, tutorsList!!.size,
                showCaseContentPosition, tintBackgroundColor, customTarget, radius
            )
        } catch (t: Throwable) {
            // do nothing
        }
    }

    fun close() {
        try {
            dismiss()
            val layout: TooltipLayout = this@TooltipDialog.view as TooltipLayout
            layout.closeTutorial()
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.stackTraceToString())
        }
    }

    fun setNextListener(
        nextListener: TooltipDialogListener.NextListener,
        nextListenerShouldUseInstanceFunc: Boolean = true
    ) {
        this.nextListener = nextListener
        this.nextListenerShouldUseInstanceFunc = nextListenerShouldUseInstanceFunc
    }

    fun setPreviousListener(
        previousListener: TooltipDialogListener.PreviousListener,
        prevListenerShouldUseInstanceFunc: Boolean = true
    ) {
        this.previousListener = previousListener
        this.prevListenerShouldUseInstanceFunc = prevListenerShouldUseInstanceFunc
    }

    fun setCompleteListener(
        completeListener: TooltipDialogListener.CompleteListener,
        completeListenerShouldUseInstanceFunc: Boolean = true
    ) {
        this.completeListener = completeListener
        this.completeListenerShouldUseInstanceFunc = completeListenerShouldUseInstanceFunc
    }

    fun setSkipListener(
        skipListener: TooltipDialogListener.SkipListener,
        skipShouldUseInstanceFunc: Boolean = true
    ) {
        this.skipListener = skipListener
        this.skipListenerShouldUseInstanceFunc = skipShouldUseInstanceFunc
    }
}