$(function () {
    aaa();

});

function aaa() {
    var $list=$("#thelist");
    var state = 'pending';//初始按钮状态

    uploader = WebUploader.create({
        swf: 'webuploader/Uploader.swf',// swf文件路径
        // fileVal: 'multiFile',  //提交到到后台，是要用"multiFile"这个名称属性来取文件的
        server: '/bbb',// 文件接收服务端。这里可以换成ccc来测试，当所有分片文件传送完成后触发的请求地址跟这里对应
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
