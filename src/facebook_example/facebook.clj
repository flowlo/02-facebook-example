(ns facebook-example.facebook
  (:gen-class)
  (:require [clojure.string :as s]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def PAGE_ACCESS_TOKEN (env :page-access-token))
(def VERIFY_TOKEN (env :verify-token))

(defn validate-webhook [request]
  (let [params (:params request)]
    (println "Incoming Webhook Request:")
    (println request)
    (if (and (= (params "hub.mode") "subscribe")
             (= (params "hub.verify_token") VERIFY_TOKEN))
        {:status 200 :body (params "hub.challenge")}
        {:status 403})))

(defn send-api [message-data]
  (println "Sending message-data:")
  (println message-data)
  (try
      (let [response (http/post "https://graph.facebook.com/v2.6/me/messages"
                      {:query-params {"access_token" PAGE_ACCESS_TOKEN}
                       :headers {"Content-Type" "application/json"}
                       :body (json/write-str message-data)
                       :insecure? true})]
        (if (= (:status @response) 200)
            (println "Successfully sent message to FB.")
            (do
              (println "Error sending message to FB:")
              (println @response))))
      (catch Exception e (do (.printStackTrace e)))))

(defn send-message [recipient-id message]
  (send-api {:recipient {:id recipient-id}
             :message message}))

(defn image-message [image-url]
  {:attachment {:type "image"
                :payload {:url image-url}}})

(defn text-message [message-text]
  {:text message-text})

(defn reply-to [messaging-event]
  "Generator for reply function. Takes a messaging event and returns a partial of send-message that uses the sender as recpient."
  (partial send-message (get-in messaging-event [:sender :id])))

(defn dispatch [messaging-event on-message on-postback on-attachments]
  ; Check for message (onMessage) or postback (onPostback) here
  (cond
    (contains? messaging-event :postback)
    (on-postback messaging-event)

    (contains? messaging-event :message)
    (if (contains? (:message messaging-event) :attachments) (on-attachments messaging-event) (on-message messaging-event))

    :else (println "Ignoring an unknown messaging event.")))

(defn handle-message [request on-message on-postback on-attachments]
  ; TODO: IMPLEMENT APP_SECRET VALIDATION
  (let [data (get-in request [:params])]
    (when (= (:object data) "page")
      (doseq [page-entry (:entry data)]
        (doseq [messaging-event (:messaging page-entry)]
          (do
            (println (str "Incoming Messaging Event: " messaging-event))
            (doseq [reply (dispatch messaging-event on-message on-postback on-attachments)]
              ((reply-to messaging-event) reply))))))))
