$(function () {
    aaa();

});

function aaa() {
    var fileMd5;
    var $list=$("#thelist");
    var state = 'pending';//初始按钮状态


    //
    // //监听分块上传过程中的三个时间点
    // WebUploader.Uploader.register({
    //     "before-send-file" : "beforeSendFile",
    //     "before-send" : "beforeSend",
    //     "after-send-file" : "afterSendFile",
    // }, {
    //     //时间点1：所有分块进行上传之前调用此函数
    //     beforeSendFile : function(file) {
    //         var deferred = WebUploader.Deferred();
    //         //1、计算文件的唯一标记，用于断点续传
    //         (new WebUploader.Uploader()).md5File(file, 0, 100 * 1024)
    //             .progress(function(percentage) {
    //                 $('#' + file.id).find("p.state").text("正在读取文件信息...");
    //             }).then(function(val) {
    //             fileMd5 = val;
    //             $('#' + file.id).find("p.state").text("成功获取文件信息...");
    //             //获取文件信息后进入下一步
    //             deferred.resolve();
    //         });
    //         return deferred.promise();
    //     },
    //     //时间点2：如果有分块上传，则每个分块上传之前调用此函数
    //     beforeSend : function(block) {
    //         var deferred = WebUploader.Deferred();
    //
    //         $.ajax({
    //             type : "POST",
    //             url : "/ccc?action=checkChunk",
    //             data : {
    //                 //文件唯一标记
    //                 fileMd5 : fileMd5,
    //                 //当前分块下标
    //                 chunk : block.chunk,
    //                 //当前分块大小
    //                 chunkSize : block.end - block.start,
    //                 enctype : "multipart/form-data"
    //             },
    //             dataType : "json",
    //             success : function(response) {
    //                 if (response.ifExist) {
    //                     //分块存在，跳过
    //                     deferred.reject();
    //                 } else {
    //                     //分块不存在或不完整，重新发送该分块内容
    //                     deferred.resolve();
    //                 }
    //             }
    //         });
    //
    //         this.owner.options.formData.fileMd5 = fileMd5;
    //         deferred.resolve();
    //         return deferred.promise();
    //     },
    //     //时间点3：所有分块上传成功后调用此函数
    //     afterSendFile : function() {
    //         //如果分块上传成功，则通知后台合并分块
    //         $.ajax({
    //             type : "POST",
    //             url : "/ccc?action=mergeChunks",
    //             data : {
    //                 fileMd5 : fileMd5,
    //             },
    //             success : function(response) {
    //                 alert("上传成功");
    //             }
    //         });
    //     }
    // });






    uploader = WebUploader.create({
        swf: 'webuploader/Uploader.swf',// swf文件路径
        // fileVal: 'multiFile',  //提交到到后台，是要用"multiFile"这个名称属性来取文件的
        server: '/bbb',// 文件接收服务端。
        // 选择文件的按钮。可选。
        // 内部根据当前运行是创建，可能是input元素，也可能是flash.
        pick: '#picker',
        fromData : {
            guid : 'guid'
        },
        resize: false,// 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！
        chunked: true, // 分块
        chunkSize: 100 * 1024, // 字节 100k分块
        threads: 3, //开启线程
        auto: false,//禁止自动上传
        disableGlobalDnd: true,// 禁掉全局的拖拽功能。这样不会出现图片拖进页面的时候，把图片打开。
    });
// 当有文件被添加进队列的时候
    uploader.on( 'fileQueued', function( file ) {
        $list.append( '<div id="' + file.id + '" class="item">' +
            '<h4 class="info">' + file.name + '</h4>' +
            '<p class="state">等待上传...</p>' +
            '</div>' );

    });

    // 文件上传过程中创建进度条实时显示。
    uploader.on( 'uploadProgress', function( file, percentage ) {
        $('#' + file.id).find('p.state').text(
        '上传中 ' + Math.round(percentage * 100) + '%');
    });
    uploader.on( 'uploadSuccess', function( file ) {
        //上传完成之后给后台发个信号，组装分片数据
        $.ajax({ url: "/bbb?isSuccess=true&fileName="+file.name,success: function(e){
                $( '#'+file.id ).find('p.state').text('已上传');
            }});
    });

    uploader.on( 'uploadError', function( file ) {
        $( '#'+file.id ).find('p.state').text('上传出错');
    });

    uploader.on( 'uploadComplete', function( file ) {
        $( '#'+file.id ).find('.progress').fadeOut();
    });
    $("#ctlBtn").on('click', function() {
        uploader.upload();
    });





}