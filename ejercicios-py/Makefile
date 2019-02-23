PYTHON_VERSION=
PYTHON_ENV_TEST=env-test$(PYTHON_VERSION)
all: clean env test-versions package

env: env/bin/activate

env/bin/activate:
	bin/env.sh

test:
	bin/tests.sh $(PYTHON_VERSION)

test-clean:
	rm -Rf env-*

clean:
	bin/clean.sh

package: clean env
	bin/package.sh

continuous-test:
	bash bin/exec-continuous-test.sh
