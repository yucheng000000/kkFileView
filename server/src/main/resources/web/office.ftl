
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>文件预览</title>

    <#--https-->
    <#--<meta http-equiv="Content-Security-Policy" content="upgrade-insecure-requests">-->
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no, minimal-ui" />
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="mobile-web-app-capable" content="yes" />
    <#include "*/commonHeader.ftl">
    <title>WINGTECH</title>
    <link rel="icon" href="css/img/word.ico" type="image/x-icon" />
    <link rel="stylesheet" type="text/css" href="css/editor.css" />
    <script type="text/javascript" src="${officeUrl}/web-apps/apps/api/documents/api.js"></script>
    <script type="text/javascript" language="javascript">
        var docEditor;
        var config = JSON.parse('${json}');
        config.width = "100%";
        config.height = "100%";
        var сonnectEditor = function () {
            docEditor = new DocsAPI.DocEditor("iframeEditor", config);
        };
        if (window.addEventListener) {
            window.addEventListener("load", сonnectEditor);
        } else if (window.attachEvent) {
            window.attachEvent("load", сonnectEditor);
        }
    </script>

</head>
    <body>

        <body>
        <div class="form">
            <div id="iframeEditor"></div>
        </div>
        </body>
    </body>

<script type="text/javascript">
    window.onload = function () {
        initWaterMark();
    }
</script>
</html>
