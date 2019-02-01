APPKEY=API認証鍵
HMKEY=API認証HM鍵
X_2ch_UA=API認証時XUA
USERAGENT=API利用時UA（認証・DAT取得）
HOST=ターゲットサーバ
BBS=ターゲット板
PORT=棒読みちゃんメインポート
HTTP_PORT=棒読みちゃんメインHTTPポート（本ツールに置いて使わないがINLINE側との競合を避ける目的で存在）
INLINE=スピーカーでの再生と同時に他入力デバイスへの再生も行いたい場合は1（true）に。主にライン入力を目的
INLINE_PORT=棒読みちゃんインラインポート
INLINE_HTTP_PORT=棒読みちゃんインラインHTTPポート（本ツールに置いて使わないがMAIN側との競合を避ける目的で存在）
NG_NUM=連続して何回同じレスが書き込まれた時にその内容をNGするか
NG_LINE=読み上げをスルーする行数（IN_LINE以上の行となる書き込みをスルー）
PAUSE_TIME=読み上げ間隔（ms）
YOMIAGE_ALL=読み上げ開始時に>>1から読み上げる(1=true)か最新レスから読み上げる(0=false)か
SUBYOMI_RES=スレ一覧監視時に更新スレの書込み内容を読み上げるか(1=true)
SUBYOMI_TITLE=スレ一覧監視時に更新スレのタイトルを読み上げるか(1=true)
SUBYOMI_AUTONG=>>1がNGListで指定されている内容の場合に自動でNGThread化するか(1=true)
KEEP_THREAD=スレ監視と同時に保守するか(1=true)
KEEP_THREAD_TIME=保守間隔(s)
KEEP_THREAD_MAXTIME=対乱立用。スレ数がKEEP_THREAD_MAXNUMを超えている場合の保守間隔(s)
KEEP_THREAD_MAXNUM = 対乱立用。乱立状態と判断するスレ数基準
KEEP_THREAD_DEPTH = 保守時間から何秒前に処理を始めるか(s)
KEEP_POST_USERAGENT=書き込み用UA（スレ保守用）