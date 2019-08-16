package sk.pego149.johnnytube

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.squareup.picasso.Picasso


//Trieda adapter pre RecyclerView s videami
class YoutubeAdapter(private val mContext: Context, private val mVideoList: List<VideoItem>) :
    RecyclerView.Adapter<YoutubeAdapter.MyViewHolder>() {
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var thumbnail: ImageView = view.findViewById(R.id.video_thumbnail) as ImageView
        var vTitle: TextView = view.findViewById(R.id.video_title) as TextView
        var vChannel: TextView = view.findViewById(R.id.video_channel_name) as TextView
        var vDescription: TextView = view.findViewById(R.id.video_description) as TextView
        var vView: RelativeLayout = view.findViewById(R.id.video_view) as RelativeLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.video_item2, parent, false)
        return MyViewHolder(itemView)
    }

    //Nahradza obsah z view.. plni kazdy item z view textami a obrazkami
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val singleVideo = mVideoList[position]

        holder.vChannel.text = android.text.Html.fromHtml(singleVideo.channel)
        holder.vTitle.text = android.text.Html.fromHtml(singleVideo.title)
        holder.vDescription.text = android.text.Html.fromHtml(singleVideo.description)

        //Picasso kniznica na upravu obrazka
        Picasso.with(mContext)
            .load(singleVideo.thumbnailURL.toString())
            .resize(480, 270)
            .centerCrop()
            .into(holder.thumbnail)

        //nastavenie on click listenera pre kazdy video_item na spustenie videa v novrej aktivite
        holder.vView.setOnClickListener{
            val intent = Intent(mContext, PlayerActivity::class.java)
            intent.putExtra("VIDEO_CHANNEL", android.text.Html.fromHtml(singleVideo.channel).toString())
            intent.putExtra("VIDEO_TITLE", android.text.Html.fromHtml(singleVideo.title).toString())
            intent.putExtra("VIDEO_DESC", android.text.Html.fromHtml(singleVideo.description).toString())
            intent.putExtra("VIDEO_ID", singleVideo.id)

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            mContext.startActivity(intent)
        }
    }

    //Getter na velkost datasetu
    override fun getItemCount(): Int {
        return mVideoList.size
    }
}