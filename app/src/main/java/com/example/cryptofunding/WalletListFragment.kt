package com.example.cryptofunding


import android.animation.Animator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cryptofunding.data.Wallet
import com.example.cryptofunding.di.injector
import com.example.cryptofunding.ui.viewholder.WalletItem
import com.example.cryptofunding.utils.DEBUG
import com.example.cryptofunding.viewmodel.viewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_wallet_list.*
import kotlinx.android.synthetic.main.fragment_wallet_list.view.*

/**
 * A simple [Fragment] subclass.
 */
class WalletListFragment : Fragment() {
    lateinit var adapter: FastAdapter<WalletItem>
    private var currentSelectedView: View? = null
    private var itemPosition: Int? = null

    private val viewModel by viewModel {
        activity!!.injector.walletListViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wallet_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        hideDetails()

        viewModel.wallets.observe(this) {
            if (it.isNotEmpty()) {
                wallet_list_nowallet.visibility = View.GONE
                setupWalletList(it)
                handleClickListener()
                wallet_list_recyclerview.adapter = this.adapter
                wallet_list_recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            else {
                wallet_list_recyclerview.visibility = View.GONE
            }
        }

        viewModel.currentWallet.observe(this) {
            showDetails()

            wallet_list_detailname.text = it.name
            wallet_list_notifamount.text = "0"
            it.amount.observe(this) { currentAmount ->
                wallet_list_amount.text = currentAmount
            }
        }

        wallet_list_addwallet_fab.setOnClickListener {
            val action = WalletListFragmentDirections.actionWalletListFragmentToNewWalletFragment2()
            view.findNavController().navigate(action)
        }
    }

    private fun setupWalletList(wallets: List<Wallet>) {
        val itemAdapter = ItemAdapter<WalletItem>()
        adapter = FastAdapter.with(itemAdapter)

        itemAdapter.add(wallets.map {
            viewModel.loadAmountIfNeeded(it)
            val item = WalletItem(it)
            if (viewModel.isCurrentWallet(it)) {
                item.isSelected = true
            }
            item
        })
    }

    private fun handleClickListener() {
        adapter.onClickListener = { view, adapter, item, index ->
            if (!viewModel.isCurrentWallet(item.wallet)) {
                itemPosition?.let {
                    adapter.getAdapterItem(it).isSelected = false
                }
                itemPosition = index
                viewModel.setCurrentWallet(item.wallet)
                deselectRow()
                item.isSelected = true
                currentSelectedView = view
                createScaleAnimation()
            }

            true
        }
    }

    private fun showDetails() {
        wallet_list_noselected.visibility = View.GONE
        wallet_list_detailname.visibility = View.VISIBLE
        wallet_list_detailname.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        wallet_list_amount.visibility = View.VISIBLE
        wallet_list_amount.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        wallet_list_available.visibility = View.VISIBLE
        wallet_list_currency.visibility = View.VISIBLE
        wallet_list_notification.visibility = View.VISIBLE
        wallet_list_notifamount.visibility = View.VISIBLE
        wallet_list_notifamount.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
    }

    private fun hideDetails() {
        wallet_list_noselected.visibility = View.VISIBLE
        wallet_list_detailname.visibility = View.GONE
        wallet_list_amount.visibility = View.GONE
        wallet_list_available.visibility = View.GONE
        wallet_list_currency.visibility = View.GONE
        wallet_list_notification.visibility = View.GONE
        wallet_list_notifamount.visibility = View.GONE
    }

    private fun setupToolbar() {
        activity?.toolbar_title?.text = getString(R.string.my_wallets)
    }

    private fun deselectRow() {
        currentSelectedView?.let {
            it.animation = null
            it.animate()
                .setDuration(50)
                .scaleY(1f)
                .scaleX(1f)
                .setStartDelay(10)
                .setInterpolator(LinearInterpolator())
                .setListener(null)
                .start()
        }
    }

    private fun createScaleAnimation() {
        currentSelectedView?.let {
            val interpolator = LinearInterpolator()
            it.animate()
                .setDuration(50)
                .scaleX(1.06f)
                .scaleY(1.06f)
                .setStartDelay(10)
                .setInterpolator(interpolator)
                .setListener(object: Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        it.animate()
                            .setDuration(100)
                            .scaleX(1.04f)
                            .scaleY(1.04f)
                            .setInterpolator(interpolator)
                            .start()
                    }

                    override fun onAnimationCancel(p0: Animator?) {

                    }

                    override fun onAnimationStart(p0: Animator?) {

                    }

                })
                .start()
        }
    }
}
