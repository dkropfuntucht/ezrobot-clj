(ns ezrobot-clj.core
  (:require [clojure.core.async :as async])
  (:import [java.net Socket]))

;; -- Bare Socket Routines
(defn connect
  [address port]
  (let [socket (Socket. address port)
        in-s  (.getInputStream socket)
        out-s (.getOutputStream socket)]
    {:socket socket
     :read-stream in-s
     :write-stream out-s}))

(defn raw-send
  [socket bytes]
  (.write (:write-stream socket) bytes)
  (.flush (:write-stream socket)))

(defn raw-read
  [socket]
  (.read (:read-stream socket)))

;; - core async connect system
(def buf-size 256)
(defn connect-async
  [address port]
  (let [socket      (connect address port)
        in-channel  (async/chan buf-size)
        out-channel (async/chan buf-size)]
    (async/go
      (while true
        (let [message (async/<! out-channel)]
          ;;TODO: socket exception handling
          (raw-send socket message))))
    (async/go
      (while true
        (if-let [byte (raw-read socket)]
          (async/>! in-channel byte))))
    (assoc socket :send-channel out-channel
                  :read-channel in-channel)))

(defn put-robot!
  "put a vector of bytes on the robot's outgoing channel"
  [robot value]
  (async/put! (:send-channel robot) (byte-array value)))

(defn get-robot!
  [robot]
  ;;TODO: read timeout
  (async/<!! (:read-channel robot)))

(defn handshake
  [robot]
  (put-robot! robot [0x55])
  (let [returned (get-robot! robot)]
    ({4   :ezrobot-V4-comm-1
       42  :ezrobot-V4-comm-2
       100 :ezrobot-V4-iotiny
       78  :already-connected
       166 :ezrobot-v3
      2   :ezrobot-v3-bootloader} returned)))

(defn get-id
  [robot]
  (put-robot! robot [0x02])
  (reduce (fn [a _] (conj a (get-robot! robot))) [] (range 0 12)))

(defn activate-port!
  [robot port-index]
  (put-robot! robot [(+ 0x64 port-index)]))

(defn move-servo!
  [robot servo-index position speed]
  (put-robot! robot [(+ 0xac servo-index) position])
  (put-robot! robot [(+ 0x27 servo-index) speed]))

(defn release-servos!
  [robot]
  (put-robot! robot [0x01]))

(defn get-temperature
  [robot]
  (put-robot! robot [0x04 0x03])
  (let [a (get-robot! robot)
        b (get-robot! robot)]
    (* 0.026341480261472 (+ a (bit-shift-left b 8)))))

(defn get-voltage
  [robot]
  (put-robot! robot [0x04 0x02])
  (let [a (get-robot! robot)
        b (get-robot! robot)]
    (* 0.003862434 (+ a (bit-shift-left b 8)))))

;;TODO: explore the position of every servo and find good defaults
;;TODO: create a map of the machine state and put it in an atom
;;TODO:
