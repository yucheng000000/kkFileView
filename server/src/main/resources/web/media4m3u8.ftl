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
    </style>
</head>
<body>
<div class="m">
    <video id="my_video_1" class="video-js vjs-default-skin" controls preload="auto"
           data-setup='{}'>
        <source src="${mediaUrl}" type="application/x-mpegURL">
    </video>

    <script>

    </script>
</div>
<script>
    // plyr.setup();
    window.onload = function () {
        initWaterMark();
    }
</script>
</body>
</html>

