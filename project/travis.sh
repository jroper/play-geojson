#!/usr/bin/env bash

[[ "$TRAVIS_TAG" != ""
&& "$TRAVIS_SECURE_ENV_VARS" == "true"
]]
on_a_tag=$?

if [[ $on_a_tag == 0 ]]; then
  PUBLISH=+publish
else
  PUBLISH=+publishLocal
fi

sbt +test "$PUBLISH"
