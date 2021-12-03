<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <meta charset="utf-8"/>
    <title>多媒体文件预览</title>
<#--<link rel="stylesheet" href="plyr/plyr.css"/>-->
<#--<script type="text/javascript" src="plyr/plyr.js"></script>-->

    <link href="css/video-js.css" rel="stylesheet">
    <script src="js/video.min.js"></script>
    <script src="js/videojs-contrib-hls.min.js"></script>

    <#include "*/commonHeader.ftl">
    <style>
        body {
            background-color: #404040;
        }

        .m {
            margin: auto;
            margin-top: 2%;
            width: 60%;
            height: 700px;
            padding: 20px;
        }
        .center {
            padding: 70px 0;
            text-align: center;
            margin: auto;
            width: 50%;
            padding: 20px;
        }
        p {
            color: aliceblue;
        }
    </style>
</head>
<body>
<#if mediaUrl == "0">
 <div class="center">
     <p id="p" >首次预览，正在转码中</p>
     <p >进度缓慢可关闭此页面，等待后台转码完成再点开预览</p>
 </div>
<#else>
    <p id="p" class="center" style="color: aliceblue;" >${fileName}</p>
    <video id="my_video_1"  class="video-js vjs-default-skin vjs-big-play-centered m" controls preload="auto"
           data-setup='{}'>
        <source src="${mediaUrl}" type="application/x-mpegURL">
    </video>
 </#if>
<script>
    window.onload = function () {
        initWaterMark();
        if("0" === "${mediaUrl}"){
            var p = $("#p");
            p.append("${m3u8Speed}"+"%")
            setTimeout("window.location.reload()",5000)
            setInterval(function(){ p.append(".");},1000);
        }
    }

</script>
</body>
</html>

