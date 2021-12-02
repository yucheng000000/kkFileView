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
            width: 1024px;
            margin: 0 auto;

        }
        .center {
            padding: 70px 0;
            text-align: center;
        }
    </style>
</head>
<body>
<#if mediaUrl == "0">
 <div class="center"><p id="p" style="color: aliceblue;" >首次预览，正在转码中</p></div>
<#else>
<div class="m">
    <video id="my_video_1" class="video-js vjs-default-skin" controls preload="auto"
           data-setup='{}'>
        <source src="${mediaUrl}" type="application/x-mpegURL">
    </video>
</div>
 </#if>
<script>
    window.onload = function () {
        initWaterMark();
        if("0" === "${mediaUrl}"){
            var p = $("#p");
            p.append(GetPercent(${m3u8Speed.targetSize?c},${m3u8Speed.sourceSize?c}))
            setTimeout("window.location.reload()",5000)
            setInterval(function(){ p.append(".");},1000);
        }
    }
    function GetPercent(num, total) {
        num = parseFloat(num);
        total = parseFloat(total);
        if (isNaN(num) || isNaN(total)) {
            return "-";
        }
        return total <= 0 ? "0%" : (Math.round(num / total * 10000) / 100.00)+"%";
    }
</script>
</body>
</html>

