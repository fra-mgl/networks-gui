cd ryu-rest
(. .venv/bin/activate && ryu-manager app.py) &
cd mn-rest
(. .venv/bin/activate && sudo flask --debug run) &
