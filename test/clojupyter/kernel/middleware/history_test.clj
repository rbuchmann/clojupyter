(ns clojupyter.kernel.middleware.history-test
  (:require
   [midje.sweet							:refer [before fact with-state-changes =>]]
   [nrepl.core					:as nrepl]
   ,,
   [clojupyter.kernel.init			:as init]
   [clojupyter.kernel.middleware		:as M]
   [clojupyter.kernel.cljsrv.nrepl-server	:as clojupyter-nrepl-server]
   [clojupyter.kernel.cljsrv.nrepl-comm		:as nrepl-comm]
   ,,
   [clojupyter.kernel.transport-test		:as TT]))


(def HIST-MSG  {:envelope
                [(byte-array [50, 49, 55, 49])],
                :delimiter "<IDS|MSG>",
                :signature "bd65a6e7077888981ea7afb4e820c20c28a8722d3d268d722c29e67fbdedc81b",
                :header {:msg_id "a71ea799e6cd4ffa886d44c7e04d5b9e",
                         :username "username",
                         :session "21717f9ca89e4dc0843fbc01f6e394b3",
                         :msg_type "history_request",
                         :version "5.2"},
                :parent-header {},
                :content {}})

(with-state-changes [(before :facts (init/init-global-state!))]
  (fact "inspect_request yields an inspect_reply"
    (with-open [nrepl-server	(clojupyter-nrepl-server/start-nrepl-server)
                nrepl-conn	(nrepl/connect :port (:port nrepl-server))]
      (let [nrepl-comm		(nrepl-comm/make-nrepl-comm nrepl-server nrepl-conn)
            H			((comp M/wrapin-bind-msgtype
                                       M/wrap-base-handlers) TT/UNHANDLED)
            ctx			(TT/test-ctx {:nrepl-comm nrepl-comm} HIST-MSG)]
        (H ctx)
        (->> ctx :transport TT/sent :req first :msgtype)))
    =>
    "history_reply"))
