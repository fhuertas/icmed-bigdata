# Python base module

## Make commands

* `make env`. Make a environment
* `make clean`. Clean the elements 
* `make test-clean`. Clean the tests
* `make test`. make the tests
* `make package`. build the artifact
* `make continuous-test TEST=<test folder> PACKAGE=<package> [ENV=<env folder>]` 

## Run commands

```bash
# Build envirnment
$ make clean env

# Activate environment
$ source env/bin/activate

# Run run exercise
(env) $ python -m ejercicios.kafka.examples.runner

# Deactivate environment
(env) $ deactivate
```


