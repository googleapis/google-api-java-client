#!/usr/bin/python
#
# Copyright (c) 2012 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.

"""Update https://code.google.com/p/google-api-java-client/wiki/APIs based on Discovery service.

Set up wiki and samples repos (first Time):

cd /tmp
hg clone https://code.google.com/p/google-api-java-client.wiki/ wiki
hg clone https://code.google.com/p/google-api-java-client.samples/ samples
cd -

Update wiki and samples repos (subsequent times):

cd /tmp/wiki && hg pull -u && cd -
cd /tmp/samples && hg pull -u && cd -

Usage:

./update_apis_wiki.py /tmp/wiki /tmp/samples

"""

import os
import re
import sys
import time
import urllib2

import simplejson

BEGIN_MARKER = """<wiki:comment>@BEGIN_GENERATED@</wiki:comment>"""
END_MARKER = """<wiki:comment>@END_GENERATED@</wiki:comment>"""

_SERVER = 'www.googleapis.com'
_SAMPLES_URL_TEMPLATE = ('http://samples.google-api-java-client.googlecode.com'
                         '/hg/%s/instructions.html')
_DISCOVERY = 'https://%s/discovery/v1' % _SERVER
_DIRECTORY = '%s/apis' % _DISCOVERY

_CODEGEN_SERVER = 'https://google-api-client-libraries.appspot.com'
_CODEGEN_SERVER_API_TEMPLATE = _CODEGEN_SERVER + '/download/library/%s/%s/java'

_DOC_URL_TEMPLATE = _CODEGEN_SERVER + (
    '/documentation/%s/%s/java/latest/index.html')
_EXPLORER_URL_TEMPLATE = 'https://developers.google.com/apis-explorer/#p/%s/%s/'
_APIS_CONSOLE_TEMPLATE = 'https://code.google.com/apis/console/?api=%s'


def main():

  if len(sys.argv) < 3:
    print '%s requires wiki_repo and samples_repo arguments' % sys.argv[0]
    sys.exit(1)
  wiki_repo = sys.argv[1]
  samples_repo = sys.argv[2]

  samples = sorted(
      [sample_name for sample_name in os.listdir(samples_repo)
       if sample_name.endswith('-sample') and
       'src' in os.listdir(os.path.join(samples_repo, sample_name))])

  api_directory = LoadApiDirectory(_DIRECTORY)
  #
  # If we want to inject in other APIs which are not in directory, they could
  # be merged in at this point.  Something like
  #  api_directory['items']['newapi'] = {
  #      'name': 'the name which must match the library jars',
  #      'version': 'v1',
  #      'title': 'The title we will sort by.',
  #      }
  #
  original = open(os.path.join(wiki_repo, 'APIs.wiki'), 'r')
  before = original.read()
  original.close()
  header_end = before.find(BEGIN_MARKER)
  header = before
  if header_end > 0:
    header = before[0:header_end]
  else:
    print 'Did not find index begin marker: %s' % BEGIN_MARKER
    sys.exit(1)

  trailer_start = before.find(END_MARKER)
  trailer = ''
  if trailer_start > 0:
    trailer = before[trailer_start:]
  else:
    print 'Did not find index end marker: %s' % END_MARKER
    sys.exit(1)

  replacement = open(os.path.join(wiki_repo, 'APIs.wiki'), 'w')
  replacement.write(header)
  replacement.write(BEGIN_MARKER)
  replacement.write('\n')
  ProcessApis(replacement, api_directory, samples)
  replacement.write(trailer)
  replacement.close()

  print '\nSuccess! Updated the wiki repository.'
  print '\nView Diff:'
  print 'cd %s && hg diff && cd -' % wiki_repo
  print '\nCommit and Push Changes:'
  print('cd %s && hg pull -u && hg commit -m "update APIs.wiki" && hg push && '
        'cd -' % wiki_repo)
  print '\nRevert Changes:'
  print 'cd %s && hg revert --no-backup APIs.wiki && cd -\n' % wiki_repo


def LoadApiDirectory(directory_url):
  directory = urllib2.urlopen(directory_url)
  return simplejson.loads(directory.read())


def GetDiscovery(api):
  discovery_url = '%s/%s' % (_DISCOVERY, api['discoveryLink'][2:])
  return GetDiscoveryFromUrl(discovery_url)


def GetDiscoveryFromUrl(discovery_url):
  try:
    discovery = urllib2.urlopen(discovery_url)
    disc = discovery.read()
  except:
    return None
  return simplejson.loads(disc)


class HeadRequest(urllib2.Request):
  def get_method(self):
    return 'HEAD'


def Retry(exception_to_check, tries=4, delay=3, backoff=2, logger=None):
  """Retry calling the decorated function using an exponential backoff."""

  def DecoRetry(f):
    """The retry decorator."""

    def FRetry(*args, **kwargs):
      """Retry the function."""
      mtries, mdelay = tries, delay
      try_one_last_time = True
      while mtries > 1:
        try:
          return f(*args, **kwargs)
        except exception_to_check, e:
          msg = '%s, Retrying in %d seconds...' % (str(e), mdelay)
          if logger:
            logger.warning(msg)
          else:
            print msg
          time.sleep(mdelay)
          mtries -= 1
          mdelay *= backoff
      if try_one_last_time:
        return f(*args, **kwargs)
      return
    return FRetry  # true decorator
  return DecoRetry


@Retry(urllib2.HTTPError, tries=3, delay=1, backoff=1)
def UrlOpenWithRetry(name, version):
  """Will retry urlopen a max of 3 times with a delay of 1s, 2s, 3s."""
  print 'Attempting to connect to: ' + _CODEGEN_SERVER_API_TEMPLATE % (
      name, version)
  return urllib2.urlopen(HeadRequest(_CODEGEN_SERVER_API_TEMPLATE % (
      name, version)))


def ProcessApis(out, apis, samples):
  for api in sorted(apis['items'],
                    key=lambda api: api.get('title', api.get('name'))):
    print api['name']
    if not api.get('preferred', 'True'):
      print 'Skipping non-preferred: %s/%s' % (api['name'], api['version'])
      continue
    discovery = GetDiscovery(api)
    if not discovery:
      print 'Skipping discovery on %s' % api
      continue
    try:
      name = discovery['name']
      version = discovery['version']
    except Exception:
      print 'Failed to get discovery on %s' % api
      continue
    # all versions of this api
    versions = [x['version'] for x in apis['items'] if x['name'] == name]
    print '  versions = %s' % versions
    title = discovery.get('title') or name
    description = discovery.get('description')
    docs = discovery.get('documentationLink')
    icon = discovery.get('icons').get('x32')
    preferred = discovery.get('preferred', True)
    if not preferred:
      print 'Skipping non-preferred: %s/%s' % (api['name'], api['version'])
      continue

    try:
      response = UrlOpenWithRetry(name, version)
    except urllib2.HTTPError, e:
      print 'Could not connect to the codegen server for %s:%s. %s' % (
          name, version, str(e))
      print 'Skipping %s:%s' % (name, version)
      continue

    # Get the zip_file_name from the content-disposition header.
    zip_file_name = dict(response.info()).get(
        'content-disposition').split('=')[1]
    p = re.compile('google-api-services-%s-%s-rev([0-9]+)-java-'
                   '([0-9]+\.[0-9]+\.[0-9]+-beta).zip' % (name, version))
    m = p.match(zip_file_name)

    rev = m.group(1)
    codegen_version = m.group(2)

    # Header
    out.write('\n----\n')
    out.write('= %s %s =\n' % (icon, WikiEscape(title)))
    out.write('*Description:* %s\n' % WikiEscape(description))
    # download
    out.write('\n');
    download_icon_url = 'https://developers.google.com/shared/images/arrow-24.png'
    zip_url = _CODEGEN_SERVER_API_TEMPLATE % (name, version)
    out.write('%s [%s Download the latest version of the library] '
              '_`[`[http://www.apache.org/licenses/LICENSE-2.0 Apache License 2.0]`]`_\n'
              % (download_icon_url, zip_url))
    # TODO: 'all versions' link once it is available in codegen server
    # Javadoc
    out.write('\n');
    doc_url = _DOC_URL_TEMPLATE % (name, version)
    out.write('[%s JavaDoc Reference]\n' % doc_url)
    # Samples
    PrintSamples(out, name, version, versions, samples)
    # Maven
    out.write('\n');
    out.write('*Maven Users*\n')
    out.write('\n');
    release_version = '%s-rev%s-%s' % (version, rev, codegen_version)
    group_id = 'com.google.apis'
    artifact_id = 'google-api-services-%s' % name
    out.write('Add the following {{{<repository>}}} and {{{<dependency>}}} sections to your '
              'pom.xml file:\n')
    out.write('{{{\n')
    out.write('<project>\n')
    out.write('  <repositories>\n')
    out.write('    ...\n')
    out.write('    <repository>\n')
    out.write('      <id>google-api-services</id>\n')
    out.write('      <url>http://google-api-client-libraries.appspot.com/'
              'mavenrepo</url>\n')
    out.write('    </repository>\n')
    out.write('    ...\n')
    out.write('  </repositories>\n')
    out.write('  <dependencies>\n')
    out.write('    ...\n')
    out.write('    <dependency>\n')
    out.write('      <groupId>%s</groupId>\n' % group_id)
    out.write('      <artifactId>%s</artifactId>\n' % artifact_id)
    out.write('      <version>%s</version>\n' % release_version)
    out.write('    </dependency>\n')
    out.write('    ...\n')
    out.write('  </dependencies>\n')
    out.write('</project>\n')
    out.write('}}}\n')
    out.write('_NOTE: the latest revision number (rev#) on the server most may be slightly higher '
              'than what\'s specified here._\n')
    # Reference
    out.write('\n');
    out.write('*Reference*\n')
    out.write('  * [DeveloperGuide Java client library Developer\'s Guide]\n')
    if docs:
      out.write('  * [%s Documentation for %s]\n' % (docs, title))
    explorer_url = _EXPLORER_URL_TEMPLATE % (name, version)
    out.write('  * [%s APIs Explorer for %s]\n' % (explorer_url, title))
    apis_console_url = _APIS_CONSOLE_TEMPLATE % name
    out.write('  * [%s APIs Console for %s]\n' % (apis_console_url, title))
  out.write('\n')


def WikiEscape(s):
  """Detect WikiSyntax (i.e. InterCaps, a.k.a. CamelCase) and escape it."""
  ret = []
  for word in s.split():
    if re.match(r'[A-Z]+[a-z]+[A-Z]', word):
      word = '!%s' % word
    ret.append(word)
  return ' '.join(ret)


def PrintSamples(out, name, version, versions, samples):
  """Look for samples and add them to the page.

  If any samples have versions, then only list the ones which match the version.
  Otherwise, list them all.
  """

  version = sorted(version)
  other_versions = [v for v in versions if v != version]
  my_samples = []
  for sample in samples:
    use_sample = sample.startswith('%s-' % name)
    for v in other_versions:
      if sample.startswith('%s-%s' % (name, v)):
        use_sample = False
        break
    if use_sample:
      my_samples.append(sample)
  if my_samples:
    out.write('\n*Samples*\n')
    for sample in my_samples:
      print '  Sample: %s' % sample
      sample_url = _SAMPLES_URL_TEMPLATE % sample
      out.write('  * [%s %s]\n' % (sample_url, sample))


main()
