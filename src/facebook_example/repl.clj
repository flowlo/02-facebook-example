(ns facebook-example.repl
  (:gen-class)
  (:require [facebook-example.bot :refer [handle-message]]
            [clj-time.core :as time]
            [clj-time.coerce :as c]
            [clojure.string :as s]
            [clojure.term.colors :as colors]))

(def indicators
  {:send   "> "
   :reply  "< "
   :action "! "})

(defn print-send []
  (print (get indicators :send)))

(defn print-reply [reply]
  (println (str (get indicators :reply) reply)))

(defn print-action [color action]
  (println (apply color (str (get indicators :action) action))))

(defn print-image [image] (print-action colors/yellow image))
(defn print-option [option] (print-action colors/cyan option))

(def empty-message
  {:sender {:id "0"}
   :recipient {:id "0"}
   :timestamp (c/to-long (time/now))})

(defn mock-text [message]
  (assoc empty-message :message {:text message}))

(defn mock-postback [payload]
  (assoc empty-message :postback {:payload payload}))

(defn print-text [response]
  (do (println (str "< " (get response :text)))))

(defn beautiful-image [response]
  (println (colors/yellow (str (get indicators :action) "Bot sends image: " (get-in response [:attachment :payload :url])))))

(defn beautiful-button [button]
  (if (= (get button :type) "postback") (println (colors/cyan (str (get indicators :action) "Option: \"" (get button :title) "\" -> " (get button :payload))))))

(defn beautiful-template [response]
  (do
    (println (str (get indicators :reply) (get-in response [:attachment :payload :text])))
    (doall (map beautiful-button (get-in response [:attachment :payload :buttons])))))

(defn beautyprint [response]
  (do (if (contains? response :text) (print-text response))
      (if (= (get-in response [:attachment :type]) "image") (beautiful-image response))
      (if (= (get-in response [:attachment :type]) "template") (beautiful-template response))))

(defn forward [message message-handler]
  (if (s/starts-with? message "^")
      (message-handler (mock-postback (subs message 1)))

      (message-handler (mock-text message))))

(defn -main []
  (while true
    (do
      (print (get indicators :send))
      (flush)
      (try
        (doall (map beautyprint (forward (read-line) handle-message)))
        (catch Exception e (do (println "\nBye!") (System/exit 0)))))))
