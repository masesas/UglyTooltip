package io.akndmr.ugly_tooltip

interface TooltipDialogListener {

    interface NextListener {
        fun onNext(index: Int, tooltip: TooltipObject?)
    }

    interface PreviousListener {
        fun onPrevious(index: Int, tooltip: TooltipObject?)
    }

    interface CompleteListener {
        fun onComplete(tooltip: TooltipObject?)
    }

    interface SkipListener {
        fun onSkip(index: Int, tooltip: TooltipObject?)
    }
}