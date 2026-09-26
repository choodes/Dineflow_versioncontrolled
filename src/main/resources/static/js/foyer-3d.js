/* ==========================================================================
   Dineflow — The Grand Hotel Foyer: Luxury Dining Cloche & Utensils 3D
   Theme: Black Obsidian & Brushed Champagne Gold
   Scene: Iconic hotel cloche dome with orbiting golden cutlery
   ========================================================================== */

(function () {
  if (typeof THREE === 'undefined') return;

  const canvas = document.getElementById('foyer-canvas');
  if (!canvas) return;

  // =========================================================================
  // Scene & Atmosphere (Deep Obsidian Void)
  // =========================================================================
  const scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0x09090C, 0.032);

  // Camera
  const camera = new THREE.PerspectiveCamera(38, window.innerWidth / window.innerHeight, 0.1, 100);
  camera.position.set(0, 1.2, 9);

  // WebGL Renderer
  const renderer = new THREE.WebGLRenderer({
    canvas: canvas,
    alpha: true,
    antialias: true,
    powerPreference: 'high-performance'
  });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.3;

  // =========================================================================
  // Theatrical Studio Lighting Rig (Noir & Champagne Gold)
  // =========================================================================

  // 1. Champagne Gold Key Light — warm dramatic side illumination
  const keyLight = new THREE.DirectionalLight(0xD4AF37, 3.0);
  keyLight.position.set(5, 7, 4);
  scene.add(keyLight);

  // 2. Cool Titanium Rim Light — editorial silhouette edge
  const rimLight = new THREE.DirectionalLight(0xCCD0E8, 2.2);
  rimLight.position.set(-5, -3, -4);
  scene.add(rimLight);

  // 3. Warm Amber Under-Glow — simulates candlelit table
  const candleGlow = new THREE.PointLight(0xF6AD55, 1.8, 12, 1.6);
  candleGlow.position.set(0, -1.5, 2);
  scene.add(candleGlow);

  // 4. Soft Spot from Above — overhead chandelier simulation
  const spotAbove = new THREE.SpotLight(0xFFF5E6, 1.2, 20, Math.PI / 6, 0.6, 1);
  spotAbove.position.set(0, 8, 0);
  spotAbove.target.position.set(0, 0, 0);
  scene.add(spotAbove);
  scene.add(spotAbove.target);

  // 5. Ambient Fill
  const fillLight = new THREE.AmbientLight(0x141420, 1.0);
  scene.add(fillLight);

  // =========================================================================
  // Materials
  // =========================================================================

  // Polished Champagne Gold — for the cloche dome
  const goldMat = new THREE.MeshPhysicalMaterial({
    color: 0xC5A059,
    emissive: 0x1A1407,
    roughness: 0.14,
    metalness: 0.94,
    clearcoat: 1.0,
    clearcoatRoughness: 0.06,
    reflectivity: 0.95
  });

  // Brushed Platinum — for the plate base
  const platinumMat = new THREE.MeshPhysicalMaterial({
    color: 0xD8D8E2,
    emissive: 0x080810,
    roughness: 0.22,
    metalness: 0.88,
    clearcoat: 0.7,
    clearcoatRoughness: 0.12,
    reflectivity: 0.85
  });

  // Dark Obsidian Chrome — for the plate underside
  const obsidianMat = new THREE.MeshStandardMaterial({
    color: 0x1A1A22,
    roughness: 0.3,
    metalness: 0.85
  });

  // Bright Gold — for utensils
  const utensilMat = new THREE.MeshPhysicalMaterial({
    color: 0xD4AF37,
    emissive: 0x0D0A02,
    roughness: 0.18,
    metalness: 0.92,
    clearcoat: 0.8,
    clearcoatRoughness: 0.1,
    reflectivity: 0.9
  });

  // =========================================================================
  // Central Piece: The Luxury Hotel Cloche (Dome Cover)
  // =========================================================================
  const clocheGroup = new THREE.Group();

  // Dome — half-sphere (the cloche cover)
  const domeGeo = new THREE.SphereGeometry(1.6, 64, 32, 0, Math.PI * 2, 0, Math.PI / 2);
  const dome = new THREE.Mesh(domeGeo, goldMat);
  dome.position.y = 0.08;
  dome.scale.set(1, 0.85, 1);
  clocheGroup.add(dome);

  // Dome Rim — thin torus ring at the base of the dome
  const domeRimGeo = new THREE.TorusGeometry(1.6, 0.045, 16, 80);
  const domeRim = new THREE.Mesh(domeRimGeo, goldMat);
  domeRim.rotation.x = Math.PI / 2;
  domeRim.position.y = 0.08;
  clocheGroup.add(domeRim);

  // Handle Knob — polished gold sphere on top
  const knobGeo = new THREE.SphereGeometry(0.14, 24, 16);
  const knob = new THREE.Mesh(knobGeo, goldMat);
  knob.position.y = 1.44;
  clocheGroup.add(knob);

  // Handle Stem — connects knob to dome
  const stemGeo = new THREE.CylinderGeometry(0.04, 0.06, 0.2, 16);
  const stem = new THREE.Mesh(stemGeo, goldMat);
  stem.position.y = 1.24;
  clocheGroup.add(stem);

  // Plate Base — flat cylinder (the serving plate)
  const plateGeo = new THREE.CylinderGeometry(2.0, 1.85, 0.12, 64);
  const plate = new THREE.Mesh(plateGeo, platinumMat);
  plate.position.y = -0.02;
  clocheGroup.add(plate);

  // Plate Edge Ring — decorative outer ring
  const plateRingGeo = new THREE.TorusGeometry(1.95, 0.035, 12, 80);
  const plateRing = new THREE.Mesh(plateRingGeo, goldMat);
  plateRing.rotation.x = Math.PI / 2;
  plateRing.position.y = 0.04;
  clocheGroup.add(plateRing);

  // Position the entire cloche
  clocheGroup.position.set(0, -0.3, -1.5);
  scene.add(clocheGroup);

  // =========================================================================
  // Orbiting Golden Cutlery (Fork, Knife, Spoon)
  // =========================================================================
  const cutleryGroup = new THREE.Group();
  cutleryGroup.position.set(0, -0.3, -1.5);

  // --- Fork ---
  const forkGroup = new THREE.Group();
  // Handle
  const forkHandle = new THREE.Mesh(
    new THREE.CylinderGeometry(0.03, 0.035, 1.1, 12),
    utensilMat
  );
  forkHandle.position.y = -0.25;
  forkGroup.add(forkHandle);
  // Tines (4 prongs)
  for (let i = -1.5; i <= 1.5; i += 1) {
    const tine = new THREE.Mesh(
      new THREE.CylinderGeometry(0.012, 0.012, 0.5, 8),
      utensilMat
    );
    tine.position.set(i * 0.028, 0.55, 0);
    forkGroup.add(tine);
  }
  forkGroup.position.set(3.2, 0.5, 0);
  forkGroup.rotation.z = -0.15;
  cutleryGroup.add(forkGroup);

  // --- Knife ---
  const knifeGroup = new THREE.Group();
  // Handle
  const knifeHandle = new THREE.Mesh(
    new THREE.CylinderGeometry(0.035, 0.04, 1.0, 12),
    utensilMat
  );
  knifeHandle.position.y = -0.2;
  knifeGroup.add(knifeHandle);
  // Blade (flattened box)
  const blade = new THREE.Mesh(
    new THREE.BoxGeometry(0.06, 0.6, 0.008),
    utensilMat
  );
  blade.position.y = 0.58;
  knifeGroup.add(blade);
  knifeGroup.position.set(-3.2, 0.5, 0);
  knifeGroup.rotation.z = 0.15;
  cutleryGroup.add(knifeGroup);

  // --- Spoon ---
  const spoonGroup = new THREE.Group();
  // Handle
  const spoonHandle = new THREE.Mesh(
    new THREE.CylinderGeometry(0.025, 0.03, 1.0, 12),
    utensilMat
  );
  spoonHandle.position.y = -0.2;
  spoonGroup.add(spoonHandle);
  // Bowl (flattened sphere)
  const spoonBowl = new THREE.Mesh(
    new THREE.SphereGeometry(0.1, 16, 12),
    utensilMat
  );
  spoonBowl.scale.set(1, 0.5, 1.4);
  spoonBowl.position.y = 0.52;
  spoonGroup.add(spoonBowl);
  spoonGroup.position.set(0, 0.8, 3.0);
  spoonGroup.rotation.x = -0.3;
  cutleryGroup.add(spoonGroup);

  scene.add(cutleryGroup);

  // =========================================================================
  // Decorative Orbital Rings (Delicate gold wire around the cloche)
  // =========================================================================
  const wireGeo = new THREE.TorusGeometry(2.8, 0.012, 12, 100);

  const orbit1 = new THREE.Mesh(wireGeo, new THREE.MeshStandardMaterial({
    color: 0xD4AF37, roughness: 0.25, metalness: 0.9
  }));
  orbit1.rotation.x = Math.PI / 2.8;
  orbit1.rotation.y = Math.PI / 7;
  orbit1.position.set(0, -0.3, -1.5);
  scene.add(orbit1);

  const orbit2 = new THREE.Mesh(
    new THREE.TorusGeometry(3.2, 0.008, 12, 100),
    new THREE.MeshStandardMaterial({ color: 0xE2E8F0, roughness: 0.35, metalness: 0.8 })
  );
  orbit2.rotation.x = -Math.PI / 3.5;
  orbit2.rotation.z = Math.PI / 5;
  orbit2.position.set(0, -0.3, -1.5);
  scene.add(orbit2);

  // =========================================================================
  // Reflective Obsidian Floor Plane
  // =========================================================================
  const floorGeo = new THREE.PlaneGeometry(40, 40, 2, 2);
  const floor = new THREE.Mesh(floorGeo, obsidianMat);
  floor.rotation.x = -Math.PI / 2;
  floor.position.y = -2.8;
  scene.add(floor);

  // =========================================================================
  // Glowing Champagne Gold Embers & Dust Motes
  // =========================================================================
  const particleCount = 80;
  const pPos = new Float32Array(particleCount * 3);
  const pSpeeds = new Float32Array(particleCount);

  for (let i = 0; i < particleCount; i++) {
    pPos[i * 3]     = (Math.random() - 0.5) * 14;
    pPos[i * 3 + 1] = (Math.random() - 0.5) * 7;
    pPos[i * 3 + 2] = (Math.random() - 0.5) * 7 - 1;
    pSpeeds[i] = 0.002 + Math.random() * 0.004;
  }

  const pGeo = new THREE.BufferGeometry();
  pGeo.setAttribute('position', new THREE.BufferAttribute(pPos, 3));

  const pMat = new THREE.PointsMaterial({
    color: 0xD4AF37,
    size: 0.04,
    transparent: true,
    opacity: 0.7,
    blending: THREE.AdditiveBlending
  });

  const particles = new THREE.Points(pGeo, pMat);
  scene.add(particles);

  // =========================================================================
  // Mouse & Gyroscope Weighted Parallax
  // =========================================================================
  let targetRotX = 0;
  let targetRotY = 0;

  window.addEventListener('mousemove', (e) => {
    const normX = (e.clientX / window.innerWidth - 0.5) * 2;
    const normY = (e.clientY / window.innerHeight - 0.5) * 2;
    targetRotY = normX * 0.35;
    targetRotX = normY * 0.25;
  }, { passive: true });

  window.addEventListener('deviceorientation', (e) => {
    if (e.gamma !== null && e.beta !== null) {
      targetRotY = Math.min(Math.max(e.gamma / 40, -1), 1) * 0.3;
      targetRotX = Math.min(Math.max((e.beta - 45) / 40, -1), 1) * 0.2;
    }
  }, { passive: true });

  window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  }, { passive: true });

  let isVisible = true;
  document.addEventListener('visibilitychange', () => {
    isVisible = !document.hidden;
    if (isVisible) requestAnimationFrame(renderLoop);
  });

  // =========================================================================
  // 60FPS Render Loop
  // =========================================================================
  const clock = new THREE.Clock();

  function renderLoop() {
    if (!isVisible) return;
    requestAnimationFrame(renderLoop);

    const t = clock.getElapsedTime();

    // Cloche: slow elegant rotation + gentle vertical bob
    clocheGroup.rotation.y = t * 0.12 + targetRotY * 0.5;
    clocheGroup.position.y = -0.3 + Math.sin(t * 0.6) * 0.08;

    // Cutlery orbit around the cloche
    cutleryGroup.rotation.y = -t * 0.15;
    // Individual utensil gentle float
    forkGroup.position.y = 0.5 + Math.sin(t * 0.9) * 0.15;
    forkGroup.rotation.z = -0.15 + Math.sin(t * 0.7) * 0.05;
    knifeGroup.position.y = 0.5 + Math.sin(t * 0.9 + 2) * 0.15;
    knifeGroup.rotation.z = 0.15 + Math.sin(t * 0.7 + 2) * 0.05;
    spoonGroup.position.y = 0.8 + Math.sin(t * 0.9 + 4) * 0.12;

    // Orbital decorative rings
    orbit1.rotation.z = t * 0.06;
    orbit1.rotation.x = Math.PI / 2.8 + Math.sin(t * 0.25) * 0.04;
    orbit2.rotation.y = -t * 0.05;

    // Camera breathing and parallax
    camera.position.x += (targetRotY * 0.5 - camera.position.x) * 0.03;
    camera.position.y += (1.2 - targetRotX * 0.4 - camera.position.y) * 0.03;
    camera.lookAt(0, 0, -1.5);

    // Golden embers drift upward
    const pos = pGeo.attributes.position.array;
    for (let i = 0; i < particleCount; i++) {
      pos[i * 3 + 1] += pSpeeds[i];
      if (pos[i * 3 + 1] > 4) {
        pos[i * 3 + 1] = -3;
      }
    }
    pGeo.attributes.position.needsUpdate = true;

    renderer.render(scene, camera);
  }

  renderLoop();
})();
