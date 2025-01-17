package io.akndmr.uglytooltip.sample

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import io.akndmr.ugly_tooltip.R.*
import io.akndmr.ugly_tooltip.TooltipBuilder
import io.akndmr.ugly_tooltip.TooltipContentPosition
import io.akndmr.ugly_tooltip.TooltipDialog
import io.akndmr.ugly_tooltip.TooltipDialogListener
import io.akndmr.ugly_tooltip.TooltipObject
import io.akndmr.uglytooltip.R

class MainActivity : AppCompatActivity() {

    private lateinit var tooltipDialog: TooltipDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUglyTooltip()

        findViewById<ImageView>(R.id.iv3).setOnClickListener {
            startUglyTooltips()
        }
    }

    private fun initUglyTooltip() {
        tooltipDialog = TooltipBuilder()
            .setPackageName(packageName)
            .titleTextColorRes(R.color.white)
            .textColorRes(R.color.white)
            .shadowColorRes(color.shadow)
            .titleTextSizeRes(dimen.title_size)
            .textSizeRes(dimen.text_normal)
            .spacingRes(dimen.spacing_normal)
            .backgroundContentColorRes(color.darker_gray)
            .circleIndicatorBackgroundDrawableRes(drawable.selector_circle)
            .prevString(string.previous)
            .nextString(nextStringText = "Next")
            .finishString(finishStringText = "Finish")
            .skipString(skipStringText = "Skip")
            .skipTextColorRes(R.color.teal_200)
            .nextTextColorRes(R.color.green)
            .finishTextColorRes(R.color.blue)
            .useCircleIndicator(true)
            .showBottomContainer(true)
            .clickable(true)
            .useArrow(true)
            .useSkipWord(true)
            .setFragmentManager(this.supportFragmentManager)
            .lineColorRes(color.line_color)
            .lineWidthRes(dimen.line_width)
            .shouldShowIcons(false)
            .setTooltipRadius(dimen.tooltip_radius)
            .showSpotlight(true)
            .build()

        tooltipDialog.setCompleteListener(object : TooltipDialogListener.CompleteListener {
            override fun onComplete(tooltip: TooltipObject?) {
                Toast.makeText(this@MainActivity, "Complete", Toast.LENGTH_SHORT).show()
            }
        })

        tooltipDialog.setSkipListener(object : TooltipDialogListener.SkipListener{
            override fun onSkip(index: Int, tooltip: TooltipObject?) {
                Toast.makeText(
                    this@MainActivity,
                    "Skip on index $index",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        tooltipDialog.setNextListener(object : TooltipDialogListener.NextListener {
            override fun onNext(index: Int, tooltip: TooltipObject?) {
                Toast.makeText(
                    this@MainActivity,
                    "Next index $index",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        tooltipDialog.setPreviousListener(object : TooltipDialogListener.PreviousListener {
            override fun onPrevious(index: Int, tooltip: TooltipObject?) {
                Toast.makeText(
                    this@MainActivity,
                    "Previous index $index text : ${tooltip?.text}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun startUglyTooltips() {
        val tooltips: ArrayList<TooltipObject> = ArrayList()

        tooltips.add(
            TooltipObject(
                findViewById<ImageView>(R.id.iv3),
                null,
                "No title, just description, simple text."
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<TextView>(R.id.tvTest2),
                null,
                "No title, just description, simple text.",
                tooltipContentPosition = TooltipContentPosition.RIGHT
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<TextView>(R.id.tvTest3),
                "<font color=\"#FFC300\"> an ImageView </font>",
                "This HTML description point to <font color=\"#FFC300\"> an ImageView </font> as you can see.<br/><br/> Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                tooltipContentPosition = TooltipContentPosition.LEFT
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<ImageView>(R.id.iv4),
                "This is a title",
                "This is a description, but a little longer than number 3 but shorter than number 5 that you will see soon."
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<TextView>(R.id.tvTest),
                "This is a title",
                "This is a description, but a little longer than number 3 but shorter than number 5 that you will see soon."
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<ImageView>(R.id.iv5),
                "This is another title",
                "This HTML description point to <font color=\"#FFC300\"> an ImageView </font> as you can see.<br/><br/> Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suo enim quisque studio maxime ducitur. Scio enim esse quosdam, qui quavis lingua philosophari possint; Animum autem reliquis rebus ita perfecit, ut corpus; Quo modo autem optimum, si bonum praeterea nullum est?"
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<ImageView>(R.id.iv6),
                "This is another one",
                "This description point to number 6. <font color=\"#FFC300\"> This is yellow text </font> and this is white.",
                tooltipContentPosition = TooltipContentPosition.UNDEFINED,
                tintBackgroundColor = ResourcesCompat.getColor(resources, R.color.blue, null),
                null
            )
        )

        tooltips.add(
            TooltipObject(
                findViewById<TextView>(R.id.tvTest4),
                "<font color=\"#FFC300\"> an ImageView </font>",
                "This HTML description point to <font color=\"#FFC300\"> an ImageView </font> as you can see.<br/><br/> Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                tooltipContentPosition = TooltipContentPosition.LEFT
            )
        )

        tooltipDialog?.show(this, supportFragmentManager, "SHOWCASE_TAG", tooltips)

    }

}